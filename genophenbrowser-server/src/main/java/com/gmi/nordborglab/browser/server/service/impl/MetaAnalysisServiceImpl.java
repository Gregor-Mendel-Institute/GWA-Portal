package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GoTerm;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.data.es.ESTermsFacet;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria;
import com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListPage;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.domain.pages.GenePage;
import com.gmi.nordborglab.browser.server.domain.pages.MetaSNPAnalysisPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.lucene.queryparser.xml.builders.RangeFilterBuilder;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.filter.FilterFacet;
import org.elasticsearch.search.facet.range.RangeFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.06.13
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */


@Service
@Transactional(readOnly = true)
public class MetaAnalysisServiceImpl implements MetaAnalysisService {

    @Resource
    protected Client client;

    @Resource
    protected StudyRepository studyRepository;

    @Resource
    protected CandidateGeneListRepository candidateGeneListRepository;

    @Resource
    protected AnnotationDataService annotationDataService;

    @Resource
    protected EsAclManager esAclManager;

    @Resource
    protected AclManager aclManager;

    @Resource
    protected AnnotationDataService annotationService;

    @Override
    public List<MetaSNPAnalysis> findAllAnalysisForRegion(int start, int end, String chr) {
        List<MetaSNPAnalysis> metaSNPAnalysises = Lists.newArrayList();
        // GET all studyids
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        FilterBuilder filter = FilterBuilders.boolFilter().must(
                FilterBuilders.hasChildFilter("meta_analysis_snps", FilterBuilders.boolFilter().
                        must(
                                FilterBuilders.numericRangeFilter("position").from(start).to(end),
                                FilterBuilders.termFilter("chr", chr)
                        )
                ),
                esAclManager.getAclFilter(Lists.newArrayList("read")));
        builder.addFields("_id", "_parent").setTypes("study").setQuery(QueryBuilders.constantScoreQuery(filter));
        SearchResponse response = builder.execute().actionGet();
        List<Long> ids = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            ids.add(Long.parseLong(hit.getId()));
        }
        if (ids.size() == 0) {
            return metaSNPAnalysises;
        }
        Iterable<Study> studies = studyRepository.findAll(ids);
        Map<Long, Study> studyCache = Maps.uniqueIndex(studies, new Function<Study, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable Study study) {
                return study.getId();
            }
        });
        // Check permission
        /*for (String id : ids) {

        } */


        // GET  all SNPs
        builder = client.prepareSearch(esAclManager.getIndex());
        filter = FilterBuilders.
                boolFilter().
                must(
                        FilterBuilders.hasParentFilter("study", FilterBuilders.idsFilter().ids(Lists.transform(ids, new Function<Long, String>() {
                            @Nullable
                            @Override
                            public String apply(@Nullable Long aLong) {
                                return String.valueOf(aLong);
                            }
                        }).toArray(new String[]{})))
                        , FilterBuilders.numericRangeFilter("position").from(start).to(end),
                        FilterBuilders.termFilter("chr", chr));
        builder.addSort("score", SortOrder.DESC).addFields("position", "mac", "maf", "_parent", "score", "overFDR", "studyid", "gene", "annotation", "inGene").setTypes("meta_analysis_snps").setQuery(QueryBuilders.constantScoreQuery(filter));
        response = builder.execute().actionGet();
        for (SearchHit searchHit : response.getHits()) {
            try {
                Map<String, SearchHitField> fields = searchHit.getFields();
                Long studyId = null;
                if (fields.containsKey("studyid")) {
                    studyId = (long) (Integer) fields.get("studyid").getValue();
                } else {
                    studyId = Long.valueOf((String) fields.get("_parent").getValue());
                }
                Study study = studyCache.get(studyId);
                SNPAnnot annot = new SNPAnnot();
                annot.setPosition((Integer) fields.get("position").getValue());
                annot.setChr(chr);
                if (fields.containsKey("annotation")) {
                    annot.setAnnotation((String) fields.get("annotation").getValue());
                    annot.setInGene((Boolean) fields.get("inGene").getValue());
                }
                MetaSNPAnalysis.Builder metaAnalysisBuilder = new MetaSNPAnalysis.Builder()
                        .setAnalysisId(studyId)
                        .setSnpAnnotation(annot)
                        .setpValue((Double) fields.get("score").getValue())
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setOverFDR((Boolean) fields.get("overFDR").getValue())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId())
                        .setMac((Integer) fields.get("mac").getValue())
                        .setMaf((Double) fields.get("maf").getValue());

                metaSNPAnalysises.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return metaSNPAnalysises;
    }


    @Override
    public List<ESFacet> findMetaStats(MetaAnalysisTopResultsCriteria criteria) {
        List<ESFacet> facets = Lists.newArrayList();
        //TODO acl filter

        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        builder.addFacet(FacetBuilders.termsFacet("chr").field("chr").size(5).order(TermsFacet.ComparatorType.TERM))
                .addFacet(FacetBuilders.rangeFacet("maf").field("maf").field("maf")
                        .addUnboundedFrom(0.01)
                        .addRange(0.01, 0.05)
                        .addRange(0.05, 0.1)
                        .addUnboundedTo(0.1))
                .addFacet(FacetBuilders.termsFacet("inGene").field("inGene").size(2))
                        //.addFacet(FacetBuilders.termsFacet("overFDR").field("overFDR").size(2))
                .addFacet(FacetBuilders.termsFacet("annotation").field("annotation").size(5));
        FilterBuilder filter = getFilterFromCriteria(criteria);
        if (filter == null) {
            builder.setQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.setQuery(QueryBuilders.constantScoreQuery(filter));
        }
        builder.setSize(0);
        SearchResponse response = builder.execute().actionGet();
        Facets searchFacets = response.getFacets();


        // get maf facet
        RangeFacet mafFacet = (RangeFacet) searchFacets.facetsAsMap().get("maf");
        List<ESTermsFacet> terms = Lists.newArrayList();
        for (RangeFacet.Entry rangeEntry : mafFacet) {
            String range = "";
            if (rangeEntry.getFrom() == Double.NEGATIVE_INFINITY) {
                range = "< " + rangeEntry.getTo() * 100 + "%";
            } else if (rangeEntry.getTo() == Double.POSITIVE_INFINITY) {
                range = "> " + rangeEntry.getFrom() * 100 + "%";
            } else {
                range = String.format("%s - %s", rangeEntry.getFrom() * 100, rangeEntry.getTo() * 100 + "%");
            }
            terms.add(new ESTermsFacet(range, rangeEntry.getCount()));
        }
        facets.add(new ESFacet("maf", 0, 0, 0, terms));

        // get chr facet
        TermsFacet searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("chr");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            terms.add(new ESTermsFacet(String.format("Chr%s", termEntry.getTerm().string()), termEntry.getCount()));
        }
        facets.add(new ESFacet("chr", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));

        // get inGene facet
        searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("inGene");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            String term = "intergenic";
            if (termEntry.getTerm().string().equalsIgnoreCase("T")) {
                term = "genic";
            }
            terms.add(new ESTermsFacet(term, termEntry.getCount()));
        }
        facets.add(new ESFacet("inGene", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));
        // get overFDR
       /* searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("overFDR");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            String term = "non-significant";
            if (termEntry.getTerm().string().equalsIgnoreCase("T")) {
                term = "significant";
            }
            terms.add(new ESTermsFacet(term, termEntry.getCount()));
        }
        facets.add(new ESFacet("overFDR", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));*/

        // get annotation
        searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("annotation");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            terms.add(new ESTermsFacet(termEntry.getTerm().string(), termEntry.getCount()));
        }
        facets.add(new ESFacet("annotation", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));

        return facets;
    }

    private FilterBuilder getFilterFromCriteria(MetaAnalysisTopResultsCriteria criteria) {
        AndFilterBuilder filter = FilterBuilders.andFilter();
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        boolFilter.must(FilterBuilders.hasParentFilter("study", esAclManager.getAclFilter(Lists.newArrayList("read"))));
        filter.add(boolFilter);
        if (criteria != null) {
            if (criteria.getChr() != null) {
                boolFilter.must(FilterBuilders.termFilter("chr", criteria.getChr()));
            }
            if (criteria.getAnnotation() != null) {
                boolFilter.must(FilterBuilders.termFilter("annotation", criteria.getAnnotation()));
            }
            if (criteria.isOverFDR() != null) {
                boolFilter.must(FilterBuilders.termFilter("overFDR", criteria.isOverFDR()));
            }
            if (criteria.isInGene() != null) {
                boolFilter.must(FilterBuilders.termFilter("inGene", criteria.isInGene()));
            }
            if (criteria.getMafFrom() != null || criteria.getMafTo() != null) {
                // use numeric because there is already a facet on it: http://elasticsearch-users.115913.n3.nabble.com/Just-Pushed-Numeric-Range-Filter-td1715331.html
                NumericRangeFilterBuilder mafFilter = FilterBuilders.numericRangeFilter("maf");
                if (criteria.getMafFrom() != null) {
                    mafFilter.gte(criteria.getMafFrom());
                }
                if (criteria.getMafTo() != null) {
                    mafFilter.lte(criteria.getMafTo());
                }
                // use and filter to combine bool and numeric_range because of performance
                //https://groups.google.com/forum/#!msg/elasticsearch/PS12RcyNSWc/I1PX1r0RfFcJ
                filter.add(mafFilter);
            }
        }

        return filter;

    }

    @Override
    public MetaSNPAnalysisPage findTopAnalysis(MetaAnalysisTopResultsCriteria criteria, int start, int size) {
        List<MetaSNPAnalysis> metaSNPAnalysises = Lists.newArrayList();
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        /*FilterBuilder filter = FilterBuilders.
                boolFilter().
                must(FilterBuilders.termFilter("chr", chr));
*/
        FilterBuilder filter = getFilterFromCriteria(criteria);
        if (filter == null) {
            builder.setQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.setQuery(QueryBuilders.constantScoreQuery(filter));
        }
        builder.setSize(size).setFrom(start).addSort("score", SortOrder.DESC).addFields("position", "mac", "maf", "chr", "score", "overFDR", "studyid", "_parent", "gene", "annotation", "inGene", "_parent").setTypes("meta_analysis_snps");
        SearchResponse response = builder.execute().actionGet();

        for (SearchHit searchHit : response.getHits()) {
            try {
                Map<String, SearchHitField> fields = searchHit.getFields();
                Long studyId = null;
                if (fields.containsKey("studyid")) {
                    studyId = (long) (Integer) fields.get("studyid").getValue();
                } else {
                    studyId = Long.parseLong((String) fields.get("_parent").getValue());
                }
                Study study = studyRepository.findOne(studyId);
                SNPAnnot annot = new SNPAnnot();
                annot.setPosition((Integer) fields.get("position").getValue());
                annot.setChr((String) fields.get("chr").getValue());
                if (fields.containsKey("annotation")) {
                    annot.setAnnotation((String) fields.get("annotation").getValue());
                    annot.setInGene((Boolean) fields.get("inGene").getValue());
                }
                if (fields.containsKey("gene")) {
                    Map<String, Object> gene = (Map<String, Object>) ((List<Map<String, Object>>) fields.get("gene").getValues().get(0)).get(0);
                    annot.setGene((String) gene.get("name"));

                }
                MetaSNPAnalysis.Builder metaAnalysisBuilder = new MetaSNPAnalysis.Builder()
                        .setAnalysisId(studyId)
                        .setSnpAnnotation(annot)
                        .setpValue((Double) fields.get("score").getValue())
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setOverFDR((Boolean) fields.get("overFDR").getValue())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId())
                        .setMac((Integer) fields.get("mac").getValue())
                        .setMaf((Double) fields.get("maf").getValue());

                metaSNPAnalysises.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new MetaSNPAnalysisPage(metaSNPAnalysises, new PageRequest(start / size, size), response.getHits().getTotalHits());
    }

    @Override
    public CandidateGeneListPage findCandidateGeneLists(ConstEnums.TABLE_FILTER filter, String searchString, int page, int size) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(page).setTypes("candidate_gene_list").setNoFields();

        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            request.setQuery(multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "description"));
        }
        FilterBuilder searchFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, false);
        FilterBuilder privateFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), true, false);
        FilterBuilder publicFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, true);

        // set facets
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.TABLE_FILTER.ALL.name()).filter(searchFilter));
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.TABLE_FILTER.PRIVATE.name()).filter(privateFilter));
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.TABLE_FILTER.PUBLISHED.name()).filter(publicFilter));

        switch (filter) {
            case PRIVATE:
                searchFilter = privateFilter;
                break;
            case PUBLISHED:
                searchFilter = publicFilter;
                break;
            case RECENT:
                request.addSort("modified", SortOrder.DESC);
                break;
            default:
                if (searchString == null || searchString.isEmpty())
                    request.addSort("name", SortOrder.ASC);
        }
        // set filter
        request.setFilter(searchFilter);

        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<CandidateGeneList> candidateGeneLists = Lists.newArrayList();
        //Neded because ids are not sorted
        Map<Long, CandidateGeneList> id2Map = Maps.uniqueIndex(candidateGeneListRepository.findAll(idsToFetch), new Function<CandidateGeneList, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable CandidateGeneList candidateGeneList) {
                return candidateGeneList.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                candidateGeneLists.add(id2Map.get(id));
            }
        }

        //extract facets
        Facets searchFacets = response.getFacets();
        List<ESFacet> facets = Lists.newArrayList();

        FilterFacet filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.TABLE_FILTER.ALL.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.ALL.name(), 0, filterFacet.getCount(), 0, null));
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.RECENT.name(), 0, filterFacet.getCount(), 0, null));

        filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.TABLE_FILTER.PRIVATE.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.PRIVATE.name(), 0, filterFacet.getCount(), 0, null));

        // get annotation
        filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.TABLE_FILTER.PUBLISHED.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.PUBLISHED.name(), 0, filterFacet.getCount(), 0, null));

        aclManager.setPermissionAndOwners(candidateGeneLists);
        return new CandidateGeneListPage(candidateGeneLists, new PageRequest(page, size), response.getHits().getTotalHits(), facets);
    }

    @Transactional(readOnly = false)
    @Override
    public CandidateGeneList saveCandidateGeneList(CandidateGeneList candidateGeneList) {
        boolean isNewRecord = candidateGeneList.getId() == null;
        candidateGeneList = candidateGeneListRepository.save(candidateGeneList);
        if (isNewRecord) {
            CumulativePermission permission = new CumulativePermission();
            permission.set(CustomPermission.ADMINISTRATION);
            permission.set(CustomPermission.EDIT);
            permission.set(CustomPermission.READ);
            aclManager.addPermission(candidateGeneList, new GrantedAuthoritySid("ROLE_ANONYMOUS"), new CustomPermission(0), null);
            aclManager.addPermission(candidateGeneList, new PrincipalSid(SecurityUtil.getUsername()),
                    permission, null);
            aclManager.addPermission(candidateGeneList, new GrantedAuthoritySid("ROLE_ADMIN"), permission, null);
        }
        candidateGeneList.setGenesWithInfo(getGeneInfos(candidateGeneList));
        indexCandidateGeneList(candidateGeneList);
        candidateGeneList = aclManager.setPermissionAndOwner(candidateGeneList);
        return candidateGeneList;
    }

    private void indexCandidateGeneList(CandidateGeneList candidateGeneList) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject()
                    .field("name", candidateGeneList.getName())
                    .field("published", candidateGeneList.getPublished())
                    .field("description", candidateGeneList.getDescription())
                    .field("modified", candidateGeneList.getModified())
                    .field("created", candidateGeneList.getCreated());
            if (candidateGeneList.getGenesWithInfo() != null && candidateGeneList.getGenesWithInfo().size() > 0) {
                builder.startArray("genes");
                for (Gene gene : candidateGeneList.getGenesWithInfo()) {
                    getGeneIndexBuilder(builder, gene);
                }
                builder.endArray();
            }
            esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl(candidateGeneList));
            builder.endObject();
            IndexRequestBuilder request = client.prepareIndex(esAclManager.getIndex(), "candidate_gene_list", candidateGeneList.getId().toString())
                    .setSource(builder).setRefresh(true);
            request.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getGeneIndexBuilder(XContentBuilder builder, Gene gene) {
        try {
            builder.startObject()
                    .field("name", gene.getName())
                    .field("symbol", gene.getSymbol())
                    .field("synonyms", gene.getSynonyms())
                    .field("chr", gene.getChr())
                    .field("start_pos", gene.getStart())
                    .field("end_pos", gene.getEnd())
                    .field("strand", gene.getStrand())
                    .field("description", gene.getDescription())
                    .field("short_description", gene.getShortDescription())
                    .field("curator_summary", gene.getCuratorSummary())
                    .field("annotation", gene.getAnnotation());
            if (gene.getGoTerms() != null && gene.getGoTerms().size() > 0) {
                builder.startArray("GO");
                for (GoTerm term : gene.getGoTerms()) {
                    getGoTermIndexBuilder(builder, term);
                }
                builder.endArray();
            }
            builder.endObject();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void getGoTermIndexBuilder(XContentBuilder builder, GoTerm term) {
        try {
            builder.startObject()
                    .field("relation", term.getRelation())
                    .field("exact", term.getExact())
                    .field("narrow", term.getNarrow())
                    .endObject();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteCandidateGeneList(CandidateGeneList candidateGeneList) {
        Long candidateGeneListId = candidateGeneList.getId();
        aclManager.setPermissionAndOwner(candidateGeneList);
        //check not public
        if (candidateGeneList.isPublic()) {
            throw new RuntimeException("Public candidateGeneLists can't be deleted");
        }
        candidateGeneListRepository.delete(candidateGeneList);
        aclManager.deletePermissions(candidateGeneList, true);
        deleteCandidateGeneListFromIndex(candidateGeneListId);
    }

    @Override
    public CandidateGeneList findOneCandidateGeneList(Long id) {
        CandidateGeneList candidateGeneList = candidateGeneListRepository.findOne(id);

        aclManager.setPermissionAndOwner(candidateGeneList);
        return candidateGeneList;
    }

    @Override
    public GenePage getGenesInCandidateGeneList(Long id, ConstEnums.GENE_FILTER filter, String searchString, int page, int size) {
        CandidateGeneList candidateGeneList = candidateGeneListRepository.findOne(id);
        List<Gene> genes = Lists.newArrayList();
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(page).setTypes("candidate_gene_list").addFields("genes");
        request.setQuery(QueryBuilders.constantScoreQuery(FilterBuilders.idsFilter().addIds(candidateGeneList.getId().toString())));
        request.addFacet(FacetBuilders.termsFacet("annotation").field("annotation").nested("genes").size(5));
        request.addFacet(FacetBuilders.termsFacet("chr").field("chr").nested("genes").size(5))
                .addFacet(FacetBuilders.termsFacet("strand").field("strand").nested("genes").size(2));


        SearchResponse response = request.execute().actionGet();
        if (response.getHits().getTotalHits() > 0) {
            SearchHit hit = response.getHits().getAt(0);
            genes = extractGeneInfos(hit);
        }
        List<ESFacet> statsFacets = Lists.newArrayList();
        List<ESTermsFacet> terms = Lists.newArrayList();

        Facets searchFacets = response.getFacets();
        List<ESFacet> facets = Lists.newArrayList();
        facets.add(new ESFacet(ConstEnums.GENE_FILTER.ALL.name(), 0, genes.size(), 0, null));
        TermsFacet annotationFacet = (TermsFacet) searchFacets.facetsAsMap().get("annotation");

        //annotation
        for (TermsFacet.Entry termEntry : annotationFacet) {
            ConstEnums.GENE_FILTER annotFilter = getFilterFromFacet(termEntry.getTerm().string());
            if (annotFilter != null) {
                facets.add(new ESFacet(annotFilter.name(), 0, termEntry.getCount(), 0, null));
            }
            terms.add(new ESTermsFacet(termEntry.getTerm().string(), termEntry.getCount()));
        }
        statsFacets.add(new ESFacet("annotation", annotationFacet.getMissingCount(), annotationFacet.getTotalCount(), annotationFacet.getOtherCount(), terms));
        //chr

        terms = Lists.newArrayList();
        TermsFacet statsTermFacet = (TermsFacet) searchFacets.facetsAsMap().get("chr");
        for (TermsFacet.Entry termEntry : statsTermFacet) {
            terms.add(new ESTermsFacet(String.format("Chr%s", termEntry.getTerm().string()), termEntry.getCount()));
        }
        statsFacets.add(new ESFacet("chr", statsTermFacet.getMissingCount(), statsTermFacet.getTotalCount(), statsTermFacet.getOtherCount(), terms));
        //strand
        terms = Lists.newArrayList();
        statsTermFacet = (TermsFacet) searchFacets.facetsAsMap().get("strand");
        for (TermsFacet.Entry termEntry : statsTermFacet) {
            terms.add(new ESTermsFacet(termEntry.getTerm().string(), termEntry.getCount()));
        }
        statsFacets.add(new ESFacet("strand", statsTermFacet.getMissingCount(), statsTermFacet.getTotalCount(), statsTermFacet.getOtherCount(), terms));

        return new GenePage(genes, new PageRequest(page, size), genes.size(), facets, statsFacets);
    }

    private ConstEnums.GENE_FILTER getFilterFromFacet(String term) {
        ConstEnums.GENE_FILTER filter = null;
        if ("gene".equalsIgnoreCase(term)) {
            filter = ConstEnums.GENE_FILTER.PROTEIN;
        } else if ("transposable_element".equalsIgnoreCase(term)) {
            filter = ConstEnums.GENE_FILTER.TRANSPOSON;
        } else if ("transposable_element_gene".equalsIgnoreCase(term)) {
            filter = ConstEnums.GENE_FILTER.TRANSPOSON;
        } else if ("pseudogene".equalsIgnoreCase(term)) {
            filter = ConstEnums.GENE_FILTER.PSEUDO;
        }
        return filter;
    }

    @Transactional(readOnly = false)
    @Override
    public Gene addGeneToCandidateGeneList(CandidateGeneList candidateGeneList, String geneId) {
        Gene gene = annotationService.getGeneById(geneId);
        if (gene != null && !candidateGeneList.getGenes().contains(geneId)) {
            candidateGeneList.setGenesWithInfo(getGeneInfos(candidateGeneList));
            candidateGeneList.getGenes().add(geneId);
            candidateGeneListRepository.save(candidateGeneList);
            candidateGeneList.getGenesWithInfo().add(gene);
            indexCandidateGeneList(candidateGeneList);
        }
        return gene;
    }

    @Transactional(readOnly = false)
    @Override
    public void removeGeneFromCandidateGeneList(CandidateGeneList candidateGeneList, String geneId) {
        candidateGeneList.getGenes().remove(geneId);
        candidateGeneListRepository.save(candidateGeneList);
        candidateGeneList.setGenesWithInfo(getGeneInfos(candidateGeneList));
        candidateGeneList.getGenesWithInfo().remove(new Gene(0, 0, 0, geneId, null));
        indexCandidateGeneList(candidateGeneList);
    }

    private void deleteCandidateGeneListFromIndex(Long candidateGeneListId) {
        client.prepareDelete(esAclManager.getIndex(), "candidate_gene_list", candidateGeneListId.toString()).execute();
    }

    private Gene getGeneById(String id) {
        Gene gene = null;
        client.prepareSearch("annot_chr1", "annot_chr2", "annot_chr3", "annot_chr4", "annot_chr5");
        return gene;
    }

    private List<Gene> extractGeneInfos(SearchHit hit) {
        List<Gene> genes = Lists.newArrayList();
        if (hit.getFields().size() > 0) {
            List<Object> fields = (List<Object>) hit.getFields().get("genes").getValues().get(0);
            for (Object geneFields : fields) {
                Map<String, Object> field = (Map<String, Object>) geneFields;
                Gene gene = new Gene((long) (Integer) field.get("start_pos"), (long) (Integer) field.get("end_pos"), (Integer) field.get("strand"), (String) field.get("name"), null);
                gene.setAnnotation((String) field.get("annotation"));
                gene.setCuratorSummary((String) field.get("curator_summary"));
                gene.setSynonyms((List<String>) field.get("synonyms"));
                gene.setDescription((String) field.get("description"));
                gene.setShortDescription((String) field.get("short_description"));
                if (field.containsKey("GO")) {
                    List<Object> goTerms = (List<Object>) field.get("GO");
                    for (Object goTermItem : goTerms) {
                        Map<String, Object> goTermFields = (Map<String, Object>) goTermItem;
                        gene.getGoTerms().add(new GoTerm((String) goTermFields.get("relation"), (String) goTermFields.get("exact"), (String) goTermFields.get("narrow")));
                    }
                }
                genes.add(gene);
            }
        }
        return genes;
    }

    private List<Gene> getGeneInfos(CandidateGeneList candidateGeneList) {
        List<Gene> genes = Lists.newArrayList();
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setTypes("candidate_gene_list").addFields("genes");
        request.setQuery(QueryBuilders.constantScoreQuery(FilterBuilders.idsFilter().addIds(candidateGeneList.getId().toString())));
        SearchResponse response = request.execute().actionGet();
        if (response.getHits().getTotalHits() > 0) {
            genes = extractGeneInfos(response.getHits().getAt(0));
        }
        return genes;
    }
}

