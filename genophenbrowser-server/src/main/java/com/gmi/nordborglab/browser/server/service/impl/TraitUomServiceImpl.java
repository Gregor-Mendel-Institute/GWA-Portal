package com.gmi.nordborglab.browser.server.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;

import javax.annotation.Resource;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadValue;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.google.common.collect.*;
import org.elasticsearch.client.Client;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Predicate;
import org.springframework.web.context.support.WebApplicationObjectSupport;

@Service
@Transactional(readOnly = true)
public class TraitUomServiceImpl extends WebApplicationObjectSupport implements TraitUomService  {
	
	@Resource
	protected Client client;

    @Resource
    private HelperService helperService;

    @Resource
    private PassportRepository passportRepository;
	
	@Resource
	private TraitUomRepository traitUomRepository;
	@Resource
	private ExperimentRepository experimentRepository;
	
	@Resource 
	private TermRepository termRepository;

	@Resource
	private MutableAclService aclService;
	
	@Resource
	private RoleHierarchy roleHierarchy;

	@Override
	public TraitUomPage findPhenotypesByExperiment(Long id, int start, int size) {
		TraitUomPage page = null;
		PageRequest pageRequest = new PageRequest(start, size);
		FluentIterable<TraitUom> traits = findPhenotypesByExperimentid(id);
		int	totalElements = traits.size();
		int pageStart = 0;
		if (start > 0)
			pageStart = start/size;
		if (totalElements > 0) {
			List<TraitUom> partitionedTraits = Iterables.get(Iterables.partition(traits, size),pageStart);
			for (TraitUom trait:partitionedTraits) {
				if (trait.getToAccession() != null) {
					trait.setTraitOntologyTerm(termRepository.findByAcc(trait.getToAccession()));
				}
			}
			page = new TraitUomPage(partitionedTraits, pageRequest,
					totalElements);
		}
		else {
			page = new TraitUomPage(traits.toImmutableList().asList(), pageRequest, 0);
		}
		return page;
	}
	
	private FluentIterable<TraitUom> findPhenotypesByExperimentid(Long id) {
		
		return filterPhenotypesByAcl(traitUomRepository.findByExperimentId(id));
	}
	
	private FluentIterable<TraitUom> filterPhenotypesByAcl(List<TraitUom> traitsToFilter) {
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList.of(BasePermission.READ);
		FluentIterable<TraitUom> traits = FluentIterable.from(traitsToFilter);
		if (traits .size() > 0) {
			final ImmutableBiMap<TraitUom,ObjectIdentity> identities = SecurityUtil.retrieveObjectIdentites(traits.toImmutableList()).inverse();
			final ImmutableMap<ObjectIdentity,Acl> acls = ImmutableMap.copyOf(aclService.readAclsById(identities.values().asList(), authorities));
			
			Predicate<TraitUom> predicate = new Predicate<TraitUom>() {

				@Override
				public boolean apply(TraitUom trait) {
					boolean flag = false;
					ObjectIdentity identity = identities.get(trait);
					if (acls.containsKey(identity)) {
						Acl acl = acls.get(identity);
						try {
							if (acl.isGranted(permissions, authorities, false)) 
								flag = true;
						}catch (NotFoundException e) {
							
						}
					}
					return flag;
				}
			};
			traits = traits.filter(predicate);
		}
		return traits;
	}
	
	

	///TODO Custom query for better performance
	@Override
	public int countPhenotypeByExperimentCount(Long id) {
		FluentIterable<TraitUom> traits = findPhenotypesByExperimentid(id);
		return traits.size();
	}

	@Override
	public TraitUom findPhenotype(Long id) {
		TraitUom traitUom = traitUomRepository.findOne(id);
		Set<StatisticType> statisticTypeToReturn = new HashSet<StatisticType>();
		List<Object[]> statisticTypes = traitUomRepository.countTraitsForStatisticType(id);
		for (Object[] statisticTypeWithCount:  statisticTypes) {
			StatisticType type = (StatisticType) statisticTypeWithCount[0];
			type.setNumberOfTraits((Long)statisticTypeWithCount[1]);
			statisticTypeToReturn.add(type);
		}
		traitUom.setStatisticTypes(statisticTypeToReturn);
		traitUom = setPermissionAndOwner(traitUom);
		if (traitUom.getToAccession() != null) {
			traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
		}
		return traitUom;
	}

	@Override
	@Transactional(readOnly = false)
	public TraitUom save(TraitUom trait) {
		if (trait.getId() == null)
			throw new RuntimeException("use create method for adding new traits");
		trait = traitUomRepository.save(trait);
//		if (trait.getId() == null) {
//			CumulativePermission permission = new CumulativePermission();
//			permission.set(BasePermission.ADMINISTRATION);
//			addPermission(experiment, new PrincipalSid(SecurityUtil.getUsername()),
//				permission);
//		}
		trait = setPermissionAndOwner(trait);
		if (trait.getToAccession() != null) {
			trait.setTraitOntologyTerm(termRepository.findByAcc(trait.getToAccession()));
		}
		return trait;
	}

	private TraitUom setPermissionAndOwner(TraitUom traitUom) {
		List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,
				traitUom.getId());
		boolean isOwner = false;
		Acl acl  = null;
		try {
			acl = aclService.readAclById(oid, authorities);
		}
		catch (NotFoundException e) {
			
		}
        if (acl != null) {
			for (Sid sid : authorities) {
				if (sid.equals(acl.getOwner())) {
					isOwner = true;
					break;
				}
			}
			boolean foundAce = false;
			for (AccessControlEntry ace: acl.getEntries()) {
				if (authorities.contains(ace.getSid())) {
					traitUom.setUserPermission(new CustomAccessControlEntry((Long)ace.getId(),ace.getPermission().getMask(),ace.isGranting()));
					foundAce = true;
					break;
				}
			}
			if (!foundAce && acl.getParentAcl() != null) {
				for (AccessControlEntry ace: acl.getParentAcl().getEntries()) {
					if (authorities.contains(ace.getSid())) {
						traitUom.setUserPermission(new CustomAccessControlEntry((Long)ace.getId(),ace.getPermission().getMask(),ace.isGranting()));
						foundAce = true;
						break;
					}
				}
			}
			
			
			
        }
		traitUom.setIsOwner(isOwner);
		traitUom.setNumberOfObsUnits(traitUomRepository.countObsUnitsByPhenotypeId(traitUom.getId()));
		traitUom.setNumberOfStudies(traitUomRepository.countStudiesByPhenotypeId(traitUom.getId()));
		return traitUom;
	}

	@Override
	public List<TraitUom> findPhenotypesByPassportId(Long passportId) {
		Sort sort = new Sort("id");
		return filterPhenotypesByAcl(traitUomRepository.findAllByPasportId(passportId,sort)).toImmutableList();
	}

	@Override
	public TraitUomPage findAll(String name, String experiment,
			String ontology, String protocol, int start, int size) {
		TraitUomPage page = null;
		PageRequest pageRequest = new PageRequest(start, size);
		Sort sort = new Sort("id");
		FluentIterable<TraitUom> traits = filterPhenotypesByAcl(traitUomRepository.findAll(sort));
		
		///TODO add fulltext search and performance improvement
		
		/*SearchRequestBuilder builder = client.prepareSearch(SearchServiceImpl.INDEX_NAME);
		PageRequest pageRequest = new PageRequest(start, size,sort);
		BooleanBuilder predicate = new BooleanBuilder();
		if (name != null || experiment != null || ontology != null || protocol != null) {
			predicate.and(localTraitNameContains(name));
		}*/
		int	totalElements = traits.size();
		int pageStart = 0;
		if (start > 0)
			pageStart = start/size;
		if (totalElements > 0) {
			List<TraitUom> partitionedTraits = Iterables.get(Iterables.partition(traits, size),pageStart);
			for (TraitUom trait:partitionedTraits) {
				if (trait.getToAccession() != null) {
					trait.setTraitOntologyTerm(termRepository.findByAcc(trait.getToAccession()));
				}
			}
			page = new TraitUomPage(partitionedTraits, pageRequest,
					totalElements);
		}
		else {
			page = new TraitUomPage(traits.toImmutableList().asList(), pageRequest, 0);
		}
		return page;
	}

    @Override
    public List<TraitUom> findPhenotypesByExperimentAndAcl(Long id, int permission) {
        final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        //TODO either change signature to always use WRITE permission or retrieve correct permission from int
        final ImmutableList<Permission> permissions = ImmutableList.of(BasePermission.WRITE);
        FluentIterable<TraitUom> traits = FluentIterable.from(traitUomRepository.findByExperimentId(id));
        if (traits.size() > 0) {
            final ImmutableBiMap<TraitUom,ObjectIdentity> identities = SecurityUtil.retrieveObjectIdentites(traits.toImmutableList()).inverse();
            final ImmutableMap<ObjectIdentity,Acl> acls = ImmutableMap.copyOf(aclService.readAclsById(identities.values().asList(), authorities));

            Predicate<TraitUom> predicate = new Predicate<TraitUom>() {

                @Override
                public boolean apply(TraitUom trait) {
                    boolean flag = false;
                    ObjectIdentity identity = identities.get(trait);
                    if (acls.containsKey(identity)) {
                        Acl acl = acls.get(identity);
                        try {
                            if (acl.isGranted(permissions, authorities, false))
                                flag = true;
                        }catch (NotFoundException e) {

                        }
                    }
                    return flag;
                }
            };
            traits = traits.filter(predicate);

            for (TraitUom traitUom:traits) {
                Set<StatisticType> statisticTypeToReturn = new HashSet<StatisticType>();
                List<Object[]> statisticTypes = traitUomRepository.countTraitsForStatisticType(traitUom.getId());
                for (Object[] statisticTypeWithCount:  statisticTypes) {
                    StatisticType type = (StatisticType) statisticTypeWithCount[0];
                    type.setNumberOfTraits((Long)statisticTypeWithCount[1]);
                    statisticTypeToReturn.add(type);
                }
                traitUom.setStatisticTypes(statisticTypeToReturn);
                traitUom = setPermissionAndOwner(traitUom);
                if (traitUom.getToAccession() != null) {
                    traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
                }
            }
        }
        return traits.toImmutableList();
    }

    @Transactional(readOnly = false)
    @Override
    public Long savePhenotypeUploadData(Long experimentId, PhenotypeUploadData data) {
        checkNotNull(data.getValueHeader());
        checkNotNull(data.getPhenotypeUploadValues());
        Experiment experiment =  experimentRepository.findOne(experimentId);
        checkNotNull(experiment);
        List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        final ImmutableList<Permission> permissions = ImmutableList.of(BasePermission.WRITE);
        ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class,
                experimentId);
        Acl acl = aclService.readAclById(oid, authorities);
        if (!acl.isGranted(permissions,authorities,false))
            throw new AccessDeniedException("No permission");
        ImmutableMap<Long,ObsUnit> lookUpForObsUnit = getObsUnitMap(experiment.getObsUnits());
        List<StatisticType> statisticTypes = getStatisticTypesFromString(data.getValueHeader());
        TraitUom traitUom = new TraitUom();
        traitUom.setLocalTraitName(data.getName());
        traitUom.setTraitProtocol(data.getProtocol());
        traitUom.setEoAccession(data.getEnvironmentOntology());
        traitUom.setToAccession(data.getTraitOntology());
        for (PhenotypeUploadValue value : data.getPhenotypeUploadValues()) {
            ObsUnit obsUnit = lookUpForObsUnit.get(value.getPassportId());
            if (obsUnit == null) {
                Passport passport = passportRepository.findOne(value.getPassportId());
                Stock stock = passport.getStocks().get(0);
                obsUnit = new ObsUnit();
                obsUnit.setStock(stock);
                obsUnit.setExperiment(experiment);
            }

            for (int i = 0;i<value.getValues().size();i++) {
                String phenValue = value.getValues().get(i);
                StatisticType statisticType = statisticTypes.get(i);
                Trait trait = new Trait();
                trait.setStatisticType(statisticType);
                trait.setValue(phenValue);
                trait.setObsUnit(obsUnit);
                traitUom.addTrait(trait);
            }
        }
        traitUom = traitUomRepository.save(traitUom);
        CumulativePermission permission = new CumulativePermission();
        permission.set(BasePermission.ADMINISTRATION);
        permission.set(BasePermission.WRITE);
        permission.set(BasePermission.READ);
        permission.set(BasePermission.DELETE);
        permission.set(BasePermission.CREATE);
        addPermission(traitUom, new PrincipalSid(SecurityUtil.getUsername()),
                permission);
        addPermission(traitUom,new GrantedAuthoritySid("ROLE_ADMIN"),permission);


        return traitUom.getId();
    }



    private List<StatisticType> getStatisticTypesFromString(List<String> valueHeader) {
        //TODO cache it
        AppData appData = helperService.getAppData();
        List<StatisticType> statisticTypes = new ArrayList<StatisticType>();
        for (int i =0;i<valueHeader.size();i++) {
            for (StatisticType type : appData.getStatisticTypeList()) {
                if (type.getStatType().equalsIgnoreCase(valueHeader.get(i))) {
                    statisticTypes.add(type);
                    break;
                }
            }
        }
        return statisticTypes;
    }

    private ImmutableMap<Long,ObsUnit> getObsUnitMap(Set<ObsUnit> obsunits) {
        Map<Long,ObsUnit> map = Maps.newHashMap();
        for (ObsUnit obsUnit:obsunits) {
            Long passportId =obsUnit.getStock().getPassport().getId();
            if (!map.containsKey(passportId))
                map.put(passportId,obsUnit);
        }
        return ImmutableMap.copyOf(map);
    }

    public void addPermission(TraitUom traitUom, Sid recipient,
                              Permission permission) {
        MutableAcl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,
                traitUom.getId());

        try {
            acl = (MutableAcl) aclService.readAclById(oid);
        } catch (NotFoundException nfe) {
            acl = aclService.createAcl(oid);
        }
        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        aclService.updateAcl(acl);
        logger.debug("Added permission " + permission + " for Sid " + recipient
                + " Phenotype " + traitUom);
    }
}

