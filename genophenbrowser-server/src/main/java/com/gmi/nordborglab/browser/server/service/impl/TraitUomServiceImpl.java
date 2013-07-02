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
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.model.Term2Term;
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
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Predicate;
import org.springframework.web.context.support.WebApplicationObjectSupport;

@Service
@Transactional(readOnly = true)
public class TraitUomServiceImpl extends WebApplicationObjectSupport implements TraitUomService {

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

    //@Resource
    //private MutableAclService aclService;

    @Resource
    private RoleHierarchy roleHierarchy;

    @Resource
    private AclManager aclManager;

    @Override
    public TraitUomPage findPhenotypesByExperiment(Long id, int start, int size) {
        TraitUomPage page = null;
        PageRequest pageRequest = new PageRequest(start, size);
        FluentIterable<TraitUom> traits = findPhenotypesByExperimentid(id);
        int totalElements = traits.size();
        int pageStart = 0;
        if (start > 0)
            pageStart = start / size;
        if (totalElements > 0) {
            List<TraitUom> partitionedTraits = Iterables.get(Iterables.partition(traits, size), pageStart);
            for (TraitUom trait : partitionedTraits) {
                if (trait.getToAccession() != null) {
                    trait.setTraitOntologyTerm(termRepository.findByAcc(trait.getToAccession()));
                }
            }
            page = new TraitUomPage(partitionedTraits, pageRequest,
                    totalElements);
        } else {
            page = new TraitUomPage(traits.toList().asList(), pageRequest, 0);
        }
        return page;
    }

    private FluentIterable<TraitUom> findPhenotypesByExperimentid(Long id) {

        return aclManager.filterByAcl(traitUomRepository.findByExperimentId(id));
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
        List<StatisticType> statisticTypeToReturn = Lists.newArrayList();
        List<Object[]> statisticTypes = traitUomRepository.countTraitsForStatisticType(id);
        for (Object[] statisticTypeWithCount : statisticTypes) {
            StatisticType type = (StatisticType) statisticTypeWithCount[0];
            type.setNumberOfTraits((Long) statisticTypeWithCount[1]);
            statisticTypeToReturn.add(type);
        }
        traitUom.setStatisticTypes(statisticTypeToReturn);
        traitUom = setStats(traitUom);
        traitUom = aclManager.setPermissionAndOwner(traitUom);
        if (traitUom.getToAccession() != null) {
            traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
        }
        return traitUom;
    }

    @Override
    @Transactional(readOnly = false)
    public TraitUom save(TraitUom traitUom) {
        if (traitUom.getId() == null)
            throw new RuntimeException("use create method for adding new traits");
        traitUom = traitUomRepository.save(traitUom);
        traitUom = setStats(traitUom);
        traitUom = aclManager.setPermissionAndOwner(traitUom);
        if (traitUom.getToAccession() != null) {
            traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
        }
        return traitUom;
    }


    private TraitUom setStats(TraitUom traitUom) {
        traitUom.setNumberOfObsUnits(traitUomRepository.countObsUnitsByPhenotypeId(traitUom.getId()));
        traitUom.setNumberOfStudies(traitUomRepository.countStudiesByPhenotypeId(traitUom.getId()));
        return traitUom;
    }

    @Override
    public List<TraitUom> findPhenotypesByPassportId(Long passportId) {
        Sort sort = new Sort("id");
        return aclManager.filterByAcl(traitUomRepository.findAllByPasportId(passportId, sort)).toList();
    }

    @Override
    public TraitUomPage findAll(String name, String experiment,
                                String ontology, String protocol, int start, int size) {
        TraitUomPage page = null;
        PageRequest pageRequest = new PageRequest(start, size);
        Sort sort = new Sort("id");
        FluentIterable<TraitUom> traits = aclManager.filterByAcl(traitUomRepository.findAll(sort));

        ///TODO add fulltext search and performance improvement

		/*SearchRequestBuilder builder = client.prepareSearch(SearchServiceImpl.INDEX_NAME);
		PageRequest pageRequest = new PageRequest(start, size,sort);
		BooleanBuilder predicate = new BooleanBuilder();
		if (name != null || experiment != null || ontology != null || protocol != null) {
			predicate.and(localTraitNameContains(name));
		}*/
        int totalElements = traits.size();
        int pageStart = 0;
        if (start > 0)
            pageStart = start / size;
        if (totalElements > 0) {
            List<TraitUom> partitionedTraits = Iterables.get(Iterables.partition(traits, size), pageStart);
            for (TraitUom trait : partitionedTraits) {
                if (trait.getToAccession() != null) {
                    trait.setTraitOntologyTerm(termRepository.findByAcc(trait.getToAccession()));
                }
            }
            page = new TraitUomPage(partitionedTraits, pageRequest,
                    totalElements);
        } else {
            page = new TraitUomPage(traits.toList().asList(), pageRequest, 0);
        }
        return page;
    }

    @Override
    public List<TraitUom> findPhenotypesByExperimentAndAcl(Long id, int permission) {
        List<TraitUom> traitsToReturn = Lists.newArrayList();
        final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        //TODO either change signature to always use WRITE permission or retrieve correct permission from int
        final ImmutableList<Permission> permissions = ImmutableList.of(CustomPermission.EDIT);
        List<TraitUom> traits = traitUomRepository.findByExperimentId(id);
        if (traits.size() > 0) {
            FluentIterable<TraitUom> traitsToFilter = aclManager.filterByAcl(traits, permissions);
            traitsToReturn = traitsToFilter.toList();
            for (TraitUom traitUom : traitsToReturn) {
                //FIXME because Hibernate returns singleton instances setting a transient field like numberofTraits will overwrite the same field for other phenotypes.
                List<Long> statisticTypeTraitCounts = Lists.newArrayList();
                List<StatisticType> statisticTypesToReturn = Lists.newArrayList();
                List<Object[]> statisticTypes = traitUomRepository.countTraitsForStatisticType(traitUom.getId());
                for (Object[] statisticTypeWithCount : statisticTypes) {
                    StatisticType type = (StatisticType) statisticTypeWithCount[0];
                    type.setNumberOfTraits((Long) statisticTypeWithCount[1]);
                    statisticTypesToReturn.add(type);
                    statisticTypeTraitCounts.add(type.getNumberOfTraits());
                }
                traitUom.setStatisticTypes(statisticTypesToReturn);
                traitUom.setStatisticTypeTraitCounts(statisticTypeTraitCounts);
                traitUom = setStats(traitUom);
                traitUom = aclManager.setPermissionAndOwner(traitUom);
                if (traitUom.getToAccession() != null) {
                    traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
                }
            }
        }
        return traitsToReturn;
    }

    @Transactional(readOnly = false)
    @Override
    public Long savePhenotypeUploadData(Long experimentId, PhenotypeUploadData data) {
        checkNotNull(data.getValueHeader());
        checkNotNull(data.getPhenotypeUploadValues());
        Experiment experiment = experimentRepository.findOne(experimentId);
        checkNotNull(experiment);
        List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        Map<Long, ObsUnit> lookUpForObsUnit = getObsUnitMap(experiment.getObsUnits());
        List<StatisticType> statisticTypes = getStatisticTypesFromString(data.getValueHeader());
        TraitUom traitUom = data.getTraitUom();
        for (PhenotypeUploadValue value : data.getPhenotypeUploadValues()) {
            ObsUnit obsUnit = lookUpForObsUnit.get(value.getPassportId());
            if (obsUnit == null) {
                Passport passport = passportRepository.findOne(value.getPassportId());
                Stock stock = passport.getStocks().get(0);
                obsUnit = new ObsUnit();
                obsUnit.setStock(stock);
                obsUnit.setExperiment(experiment);
                lookUpForObsUnit.put(value.getPassportId(), obsUnit);
            }

            for (int i = 0; i < value.getValues().size(); i++) {
                String phenValue = value.getValues().get(i);
                if (phenValue.equals("") || phenValue.equals("NA"))
                    continue;
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
        permission.set(CustomPermission.ADMINISTRATION);
        permission.set(CustomPermission.EDIT);
        permission.set(CustomPermission.READ);
        aclManager.addPermission(traitUom, new PrincipalSid(SecurityUtil.getUsername()),
                permission, experimentId);
        aclManager.addPermission(traitUom, new GrantedAuthoritySid("ROLE_ADMIN"), permission, experimentId);
        return traitUom.getId();
    }

    @Override
    public List<TraitUom> findAllByOntology(String type, String acc, boolean checkChilds) {
        List<TraitUom> traits = null;
        List<String> ontologyTerms = getOntologyList(acc, checkChilds);
        if ("trait".equalsIgnoreCase(type)) {
            traits = traitUomRepository.findAllByToAccessionIn(ontologyTerms);
        } else if ("environment".equalsIgnoreCase(type)) {
            traits = traitUomRepository.findAllByEoAccessionIn(ontologyTerms);
        } else {
            throw new RuntimeException(type + " Type unknown");
        }
        for (TraitUom trait : traits) {
            if (trait.getToAccession() != null) {
                trait.setTraitOntologyTerm(termRepository.findByAcc(trait.getToAccession()));
            }
        }
        return traits;
    }


    private List<String> getOntologyList(String acc, boolean checkChilds) {
        List<String> list = Lists.newArrayList(acc);
        if (checkChilds) {
            Term term = termRepository.findByAcc(acc);
            addChildOntologies(term, list);
        }
        return list;
    }

    private void addChildOntologies(Term term, List<String> list) {
        if (term == null) {
            return;
        }
        list.add(term.getAcc());
        for (Term2Term term2Term : term.getChilds()) {
            addChildOntologies(term2Term.getChild(), list);
        }
    }

    private List<StatisticType> getStatisticTypesFromString(List<String> valueHeader) {
        //TODO cache it
        AppData appData = helperService.getAppData();
        List<StatisticType> statisticTypes = new ArrayList<StatisticType>();
        for (int i = 0; i < valueHeader.size(); i++) {
            for (StatisticType type : appData.getStatisticTypeList()) {
                if (type.getStatType().equalsIgnoreCase(valueHeader.get(i))) {
                    statisticTypes.add(type);
                    break;
                }
            }
        }
        return statisticTypes;
    }

    private Map<Long, ObsUnit> getObsUnitMap(Set<ObsUnit> obsunits) {
        Map<Long, ObsUnit> map = Maps.newHashMap();
        for (ObsUnit obsUnit : obsunits) {
            Long passportId = obsUnit.getStock().getPassport().getId();
            if (!map.containsKey(passportId))
                map.put(passportId, obsUnit);
        }
        return map;
    }
}

