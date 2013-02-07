package com.gmi.nordborglab.browser.server.service.impl;

import static com.gmi.nordborglab.browser.server.domain.specifications.TraitUomPredicates.localTraitNameContains;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.pages.PassportPage;
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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.mysema.query.BooleanBuilder;

@Service
@Transactional(readOnly = true)
public class TraitUomServiceImpl implements TraitUomService {
	
	@Resource
	protected Client client;
	
	
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

        }
        return traits.toImmutableList();
    }
}

