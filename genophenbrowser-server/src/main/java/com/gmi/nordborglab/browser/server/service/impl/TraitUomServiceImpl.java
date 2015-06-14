package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.DomainFunctions;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.es.EsIndexer;
import com.gmi.nordborglab.browser.server.es.EsSearcher;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.StatisticTypeRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.model.Term2Term;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Service
@Transactional(readOnly = true)
public class TraitUomServiceImpl extends WebApplicationObjectSupport implements TraitUomService {


    @Resource
    private HelperService helperService;

    @Resource
    private TraitUomRepository traitUomRepository;

    @Resource
    private CdvService cdvService;

    @Resource
    private StudyRepository studyRepository;

    @Resource
    private TermRepository termRepository;

    @Resource
    private RoleHierarchy roleHierarchy;

    @Resource
    private AclManager aclManager;


    @Resource
    private EsIndexer esIndexer;

    @Resource
    private EsSearcher esSearcher;

    @Resource
    private EsAclManager esAclManager;

    @Resource
    private StatisticTypeRepository statisticTypeRepository;

    @Resource
    private PassportRepository passportRepository;

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
                initOntologies(trait);
            }
            page = new TraitUomPage(partitionedTraits, pageRequest,
                    totalElements, null);
        } else {
            page = new TraitUomPage(traits.toList().asList(), pageRequest, 0, null);
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
        initOntologies(traitUom);
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
        initOntologies(traitUom);
        indexTraitUom(traitUom);
        return traitUom;
    }

    @Transactional(readOnly = false)
    @Override
    public void delete(TraitUom traitUom) {
        aclManager.setPermissionAndOwner(traitUom);
        if (traitUom.isPublic()) {
            throw new RuntimeException("Public phenotypes can't be deleted");
        }
        List<Study> studies = studyRepository.findByPhenotypeId(traitUom.getId());
        for (Study study : studies) {
            cdvService.delete(study);
        }
        for (Trait trait : traitUom.getTraits()) {
            trait.setTraitUom(null);
        }
        traitUomRepository.delete(traitUom);
        aclManager.deletePermissions(traitUom, true);
        deleteFromIndex(traitUom);
    }

    private void deleteFromIndex(TraitUom traitUom) {
        esIndexer.delete(traitUom);
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
    public TraitUomPage findAll(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        return findAll(null, filter, searchString, start, size);
    }

    @Override
    public TraitUomPage findAll(Long experimentId, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        SearchResponse response = esSearcher.search(filter, experimentId, false, new String[]{"local_trait_name^3.5", "local_trait_name.partial^1.5", "trait_protocol", "to_accession.term_id^3.5", "to_accession.term_name^1.5", "eo_accession.term_id^3.5", "eo_accession.term_name^1.5", "owner.name", "experiment.name"}, searchString, TraitUom.ES_TYPE, start, size);
        List<Long> idsToFetch = EsSearcher.getIdsFromResponse(response);
        List<TraitUom> resultsFromDb = traitUomRepository.findAll(idsToFetch);
        //extract facets
        List<ESFacet> facets = EsSearcher.getAggregations(response);
        Ordering<TraitUom> orderByEs = Ordering.explicit(idsToFetch).onResultOf(DomainFunctions.getTraitUomId());
        List<TraitUom> results = orderByEs.immutableSortedCopy(resultsFromDb);
        for (TraitUom traitUom : results) {
            initOntologies(traitUom);
        }
        aclManager.setPermissionAndOwners(results);
        return new TraitUomPage(results, new PageRequest(start, size), response.getHits().getTotalHits(), facets);
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
                initOntologies(traitUom);
            }
        }
        return traitsToReturn;
    }

    @Transactional(readOnly = false)
    @Override
    public List<TraitUom> savePhenotypeUploadData(Experiment experiment, List<PhenotypeUploadData> data, List<SampleData> samples) {
        checkNotNull(experiment);
        checkNotNull(samples);
        checkNotNull(data);
        checkArgument(experiment.getId() != null);
        StatisticType statisticType = statisticTypeRepository.findOne(1L);
        checkNotNull(statisticType);
        for (SampleData sample : samples) {
            if (sample.hasError()) {
                continue;
            }
            Passport passport = passportRepository.findOne(sample.getPassportId());
            Stock stock = passport.getStocks().get(0);
            ObsUnit obsUnit = new ObsUnit();
            obsUnit.setStock(stock);
            obsUnit.setExperiment(experiment);
            for (int i = 0; i < data.size(); i++) {
                String phenotypeValue = sample.getValues().get(i);
                if (phenotypeValue == null || phenotypeValue.isEmpty())
                    continue;
                TraitUom traitUom = data.get(i).getTraitUom();
                Trait trait = new Trait();
                trait.setStatisticType(statisticType);
                trait.setValue(phenotypeValue);
                trait.setObsUnit(obsUnit);
                traitUom.addTrait(trait);
            }
        }
        for (PhenotypeUploadData phenotypeUploadData : data) {
            TraitUom traitUom = phenotypeUploadData.getTraitUom();
            traitUomRepository.save(traitUom);
            initOntologies(traitUom);
            CumulativePermission permission = new CumulativePermission();
            permission.set(CustomPermission.ADMINISTRATION);
            permission.set(CustomPermission.EDIT);
            permission.set(CustomPermission.READ);
            aclManager.addPermission(traitUom, new PrincipalSid(SecurityUtil.getUsername()),
                    permission, Experiment.class, experiment.getId());
            aclManager.addPermission(traitUom, new GrantedAuthoritySid("ROLE_ADMIN"), permission, Experiment.class, experiment.getId());
            indexTraitUom(traitUom);
        }
        return Lists.newArrayList(Iterables.transform(data, new Function<PhenotypeUploadData, TraitUom>() {
            @Override
            public TraitUom apply(PhenotypeUploadData input) {
                Preconditions.checkNotNull(input);
                Preconditions.checkNotNull(input.getTraitUom());
                return input.getTraitUom();
            }
        }));
    }


    private void indexTraitUom(TraitUom traitUom) {
        try {
            esIndexer.index(traitUom);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            initOntologies(trait);
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

    private void initOntologies(TraitUom traitUom) {
        if (traitUom.getToAccession() != null) {
            traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
        }
        if (traitUom.getEoAccession() != null) {
            traitUom.setEnvironOntologyTerm(termRepository.findByAcc(traitUom.getEoAccession()));
        }
    }
}

