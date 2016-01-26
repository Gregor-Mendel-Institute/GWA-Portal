package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GoTerm;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnotation;
import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.data.es.ESTermsFacet;
import com.gmi.nordborglab.browser.server.domain.DomainFunctions;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.meta.Association;
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysis;
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListEnrichmentPage;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListPage;
import com.gmi.nordborglab.browser.server.domain.pages.GenePage;
import com.gmi.nordborglab.browser.server.domain.pages.MetaAnalysisPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.es.EsIndexer;
import com.gmi.nordborglab.browser.server.es.EsSearcher;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryAction;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.hasChildQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.06.13
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */


@Service("MetaAnalysisServiceImpl")
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

    @Resource
    protected EsSearcher esSearcher;

    @Resource
    protected EsIndexer esIndexer;


    @Resource
    protected CdvService cdvService;

    @Resource
    protected CandidateGeneListEnrichmentRepository candidateGeneListEnrichmentsRepository;


    private static Logger logger = LoggerFactory.getLogger(MetaAnalysisService.class);

    // MAX  number of top assocs for grouped analysis
    private final static int TOP_ASSOC_SIZE = 20;

    @Override
    public MetaAnalysisPage findAllAnalysisForRegion(int startPos, int endPos, String chr, int start, int size, List<FilterItem> filterItems, boolean isGrouped) {
        if (!isGrouped) {
            return findFlatAnalysisForRegion(startPos, endPos, chr, start, size, filterItems);
        }
        return findGroupedAnalysisForRegion(startPos, endPos, chr, start, size, filterItems);
    }


    private MetaAnalysisPage findFlatAnalysisForRegion(int startPos, int endPos, String chr, int start, int size, List<FilterItem> filterItems) {
        List<MetaAnalysis> metaAnalyses = Lists.newArrayList();
        // GET  all SNPs
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        BoolQueryBuilder filter = QueryBuilders.boolQuery()
                .filter(QueryBuilders.hasParentQuery("study", esAclManager.getAclFilterForPermissions(Lists.newArrayList("read"))))
                .filter(QueryBuilders.rangeQuery("position").from(startPos).to(endPos))
                .filter(QueryBuilders.termQuery("chr", chr));
        QueryBuilder filterItemFilter = getFilterFromFilterItems(filterItems);
        if (filterItemFilter != null) {
            filter.filter(filterItemFilter);
        }
        builder.setSize(size).setFrom(start).addSort("score", SortOrder.DESC).addFields("position", "mac", "maf", "_parent", "score", "overFDR", "studyid", "annotation", "inGene").setTypes("meta_analysis_snps").setQuery(QueryBuilders.constantScoreQuery(filter));
        SearchResponse response = builder.execute().actionGet();
        for (SearchHit searchHit : response.getHits()) {
            try {
                Map<String, SearchHitField> fields = searchHit.getFields();
                Long studyId = null;
                if (fields.containsKey("studyid")) {
                    studyId = (long) (Integer) fields.get("studyid").getValue();
                } else {
                    studyId = Long.valueOf((String) fields.get("_parent").getValue());
                }
                //TODO Optimize by fetching all studies in one query
                Study study = studyRepository.findOne(studyId);
                MetaAnalysis.Builder metaAnalysisBuilder = MetaAnalysis.builder()
                        .setAnalysisId(studyId)
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId())
                        .setAssociations(ImmutableList.of(getAssociation(fields, false, chr)));

                metaAnalyses.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                logger.warn("Failed to retrieve associations in a region", e);
            }
        }
        return new MetaAnalysisPage(metaAnalyses, new PageRequest(start / size, size), response.getHits().getTotalHits());
    }

    private MetaAnalysisPage findGroupedAnalysisForRegion(int startPos, int endPos, String chr, int start, int size, List<FilterItem> filterItems) {

        List<MetaAnalysis> metaAnalyses = Lists.newArrayList();
        // GET  all SNPs
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());

        BoolQueryBuilder regionFilter = boolQuery()
                .must(QueryBuilders.rangeQuery("position").from(startPos).to(endPos))
                .must(QueryBuilders.termQuery("chr", chr));

        //TODO support this
        QueryBuilder filterItemFilter = getFilterFromFilterItems(filterItems);
        if (filterItemFilter != null) {
            regionFilter.filter(filterItemFilter);
        }

        FunctionScoreQueryBuilder scoreQuery = QueryBuilders.functionScoreQuery(regionFilter, ScoreFunctionBuilders.fieldValueFactorFunction("score"));
        BoolQueryBuilder query = boolQuery()
                .filter(esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")))
                .must(hasChildQuery("meta_analysis_snps", scoreQuery).scoreMode("avg")
                        .innerHit(new QueryInnerHitBuilder().setSize(TOP_ASSOC_SIZE)
                                .field("chr").field("position").field("annotation")
                                .field("annotation.gene_name").field("score").field("overFDR").field("mac").field("maf")));
        builder.setSize(size).setFrom(start).setTypes("study").setQuery(query).addFields("name", "phenotype.id", "phenotype.name", "experiment.id", "experiment.name", "protocol.analysis_method", "genotype.name");

        SearchResponse response = builder.execute().actionGet();
        for (SearchHit searchHit : response.getHits()) {
            try {
                Map<String, SearchHitField> fields = searchHit.getFields();
                MetaAnalysis.Builder metaAnalysisBuilder = MetaAnalysis.builder()
                        .setAnalysisId(Long.valueOf(searchHit.getId()))
                        .setAnalysis((String) fields.get("name").getValue())
                        .setPhenotype((String) fields.get("phenotype.name").getValue())
                        .setStudy((String) fields.get("experiment.name").getValue())
                        .setMethod((String) fields.get("protocol.analysis_method").getValue())
                        .setGenotype((String) fields.get("genotype.name").getValue())
                        .setPhenotypeId(Long.valueOf((Integer) fields.get("phenotype.id").getValue()))
                        .setStudyId(Long.valueOf((Integer) fields.get("experiment.id").getValue()))
                        .setTotalAssocCount(searchHit.getInnerHits().get("meta_analysis_snps").getTotalHits())
                        .setAssociations(ImmutableList.copyOf(getAssociations(searchHit.getInnerHits().get("meta_analysis_snps"))));
                metaAnalyses.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                logger.warn("Failed to retrieve associations in a region", e);
            }
        }
        return new MetaAnalysisPage(metaAnalyses, new PageRequest(start / size, size), response.getHits().getTotalHits(), TOP_ASSOC_SIZE);
    }


    private Association getAssociation(Map<String, SearchHitField> fields, boolean fetchGene, String chr) {
        SNPInfo info = new SNPInfo();
        info.setPosition((Integer) fields.get("position").getValue());
        if (chr == null) {
            info.setChr((String) fields.get("chr").getValue());
        }
        String annotationString = null;
        // Required because 250k SNPs are not indexed
        if (fields.containsKey("inGene")) {
            info.setInGene((Boolean) fields.get("inGene").getValue());
        }
        // Required because 250k SNPs are not indexed
        if (fields.containsKey("annotation")) {
            annotationString = fields.get("annotation").getValue();
        }

        info.setAnnotations(convertOldAnnotationTonewFormat(annotationString));
        if (fetchGene && fields.containsKey("annotations.gene_name")) {
            info.setGene((String) fields.get("annotations.gene_name").getValue());

        }
        Association.Builder associationBuilder = Association.builder()
                .setSnpInfo(info)
                .setPValue((Double) fields.get("score").getValue())
                .setOverFDR((Boolean) fields.get("overFDR").getValue());
        if (fields.containsKey("mac")) {
            associationBuilder.setMac((Integer) fields.get("mac").getValue());
        }
        if (fields.containsKey("maf")) {
            associationBuilder.setMaf((Double) fields.get("maf").getValue());
        }
        return associationBuilder.build();
    }

    private List<Association> getAssociations(SearchHits hits) {
        List<Association> associations = Lists.newArrayList();
        for (SearchHit hit : hits) {
            associations.add(getAssociation(hit.getFields(), false, null));
        }
        return associations;
    }


    @Override
    public List<ESFacet> findMetaStats(MetaAnalysisTopResultsCriteria criteria, List<FilterItem> filterItems) {
        List<ESFacet> facets = Lists.newArrayList();

        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        builder.addAggregation(AggregationBuilders.terms("chr").field("chr").size(5).order(Terms.Order.term(true)))
                .addAggregation(AggregationBuilders.range("maf").field("maf")
                        .addUnboundedFrom(0.01)
                        .addRange(0.01, 0.05)
                        .addRange(0.05, 0.1)
                        .addUnboundedTo(0.1))
                .addAggregation(AggregationBuilders.terms("inGene").field("inGene").size(2))
                .addAggregation(AggregationBuilders.terms("annotation").field("annotation").size(5));
        QueryBuilder filter = getFilterFromCriteria(criteria, filterItems);
        if (filter == null) {
            builder.setQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.setQuery(QueryBuilders.constantScoreQuery(filter));
        }
        builder.setSize(0);
        SearchResponse response = builder.execute().actionGet();
        Aggregations aggregations = response.getAggregations();

        // get maf facet
        Range mafAgg = aggregations.get("maf");
        List<ESTermsFacet> terms = Lists.newArrayList();
        for (Range.Bucket rangeEntry : mafAgg.getBuckets()) {
            String range = "";
            if (new Double(Double.NEGATIVE_INFINITY).equals(rangeEntry.getFrom())) {
                range = "< " + (Double) rangeEntry.getTo() * 100 + "%";
            } else if (new Double(Double.POSITIVE_INFINITY).equals(rangeEntry.getTo())) {
                range = "> " + (Double) rangeEntry.getFrom() * 100 + "%";
            } else {
                range = String.format("%s - %s", (Double) rangeEntry.getFrom() * 100, (Double) rangeEntry.getTo() * 100 + "%");
            }
            terms.add(new ESTermsFacet(range, rangeEntry.getDocCount()));
        }
        facets.add(new ESFacet("maf", 0, 0, 0, terms));


        // get chr facet
        Terms searchFacet = aggregations.get("chr");
        terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : searchFacet.getBuckets()) {
            terms.add(new ESTermsFacet(String.format("Chr%s", termEntry.getKeyAsString()), termEntry.getDocCount()));
        }
        facets.add(new ESFacet("chr", 0, 0, 0, terms));

        // get inGene facet
        searchFacet = aggregations.get("inGene");
        terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : searchFacet.getBuckets()) {
            String term = "intergenic";
            if (((Long) termEntry.getKey()) == 1) {
                term = "genic";
            }
            terms.add(new ESTermsFacet(term, termEntry.getDocCount()));
        }
        facets.add(new ESFacet("inGene", 0, 0, 0, terms));

        // get annotation
        searchFacet = aggregations.get("annotation");
        terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : searchFacet.getBuckets()) {
            terms.add(new ESTermsFacet(termEntry.getKeyAsString(), termEntry.getDocCount()));
        }
        facets.add(new ESFacet("annotation", 0, 0, 0, terms));

        return facets;
    }

    //TODO optimize filter order (bool vs and)   http://www.elasticsearch.org/blog/all-about-elasticsearch-filter-bitsets/
    private QueryBuilder getFilterFromCriteria(MetaAnalysisTopResultsCriteria criteria, List<FilterItem> filterItems) {
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        filter.filter(QueryBuilders.hasParentQuery("study", esAclManager.getAclFilterForPermissions(Lists.newArrayList("read"))));
        if (criteria != null) {
            if (criteria.getChr() != null) {
                filter.filter(QueryBuilders.termQuery("chr", criteria.getChr()));
            }
            if (criteria.getAnnotation() != null) {
                filter.filter(QueryBuilders.termQuery("annotation", criteria.getAnnotation()));
            }
            if (criteria.isOverFDR() != null) {
                filter.filter(QueryBuilders.termQuery("overFDR", criteria.isOverFDR()));
            }
            if (criteria.isInGene() != null) {
                filter.filter(QueryBuilders.termQuery("inGene", criteria.isInGene()));
            }
            if (criteria.getMafFrom() != null || criteria.getMafTo() != null) {
                // use numeric because there is already a facet on it: http://elasticsearch-users.115913.n3.nabble.com/Just-Pushed-Numeric-Range-Filter-td1715331.html
                RangeQueryBuilder mafFilter = QueryBuilders.rangeQuery("maf");
                if (criteria.getMafFrom() != null) {
                    mafFilter.gte(criteria.getMafFrom());
                }
                if (criteria.getMafTo() != null) {
                    mafFilter.lte(criteria.getMafTo());
                }
                // use and filter to combine bool and numeric_range because of performance
                //https://groups.google.com/forum/#!msg/elasticsearch/PS12RcyNSWc/I1PX1r0RfFcJ
                filter.filter(mafFilter);
            }
        }

        QueryBuilder filterItemsFilter = getFilterFromFilterItems(filterItems);
        if (filterItemsFilter != null) {
            filter.filter(filterItemsFilter);
        }

        return filter;

    }


    private QueryBuilder getFilterFromFilterItems(List<FilterItem> filterItems) {
        if (filterItems != null && filterItems.size() > 0) {
            BoolQueryBuilder andFilter = QueryBuilders.boolQuery();
            for (FilterItem filterItem : filterItems) {
                QueryBuilder filterItemFilter = getFilterFromFilterItem(filterItem);
                if (filterItemFilter != null)
                    andFilter.filter(filterItemFilter);
            }
            return andFilter;
        }
        return null;
    }

    private QueryBuilder getFilterFromFilterItem(FilterItem filterItem) {
        BoolQueryBuilder itemFilterBuilder = null;
        switch (filterItem.getType()) {
            case METHOD:
                itemFilterBuilder = QueryBuilders.boolQuery();
                for (FilterItemValue value : filterItem.getValues()) {
                    itemFilterBuilder.should(getQueryBuilder(QueryBuilders.termQuery("protocol.analysis_method", value.getText()), "study"));
                }
                break;
            case GENOTYPE:
                itemFilterBuilder = QueryBuilders.boolQuery();
                for (FilterItemValue value : filterItem.getValues()) {
                    itemFilterBuilder.should(getQueryBuilder(QueryBuilders.termQuery("genotype.name", value.getText()), "study"));
                }
                break;
            case STUDY:
                itemFilterBuilder = QueryBuilders.boolQuery();
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {
                        itemFilterBuilder.should(getQueryBuilder(QueryBuilders.termQuery("experiment.id", value.getValue()), "study"));
                    } else {
                        itemFilterBuilder.should(getQueryBuilder(QueryBuilders.hasParentQuery("phenotype",
                                QueryBuilders.hasParentQuery("experiment", QueryBuilders.matchQuery("name", value.getText()))), "study"));
                        //TODO can't access experiment.name in study because it is not re-indexed when name changes in experiment
                        //itemFilterBuilder.should(QueryBuilders.hasParentQuery("study", QueryBuilders.matchQuery("experiment.name", value.getText())));
                    }
                }
                break;
            case PHENOTYPE:
                itemFilterBuilder = QueryBuilders.boolQuery();
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {
                        itemFilterBuilder.should(getQueryBuilder(QueryBuilders.termQuery("phenotype.id", value.getValue()), "study"));
                    } else {

                        itemFilterBuilder.should(getQueryBuilder(QueryBuilders.hasParentQuery("phenotype", QueryBuilders.matchQuery("local_trait_name", value.getText())), "study"));
                        //TODO can't access phenotype.name in study because it is not re-indexed when name changes in phenotype
                        //itemFilterBuilder.should(QueryBuilders.hasParentQuery("study",QueryBuilders.matchQuery("phenotype.name", value.getText())));
                    }
                }
                break;
            case ANALYSIS:
                itemFilterBuilder = QueryBuilders.boolQuery();
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {
                        itemFilterBuilder.should(QueryBuilders.termQuery("studyid", value.getValue()));
                    } else {
                        itemFilterBuilder.should(getQueryBuilder(QueryBuilders.termQuery("name", value.getText()), "study"));
                    }
                }
                break;
            case CANDIDATE_GENE_LIST:
                itemFilterBuilder = QueryBuilders.boolQuery();
                String range = "";
                boolean hasCandidateGeneLists = false;
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {
                        List<Gene> genes = getCandidateGeneListRanges(value.getValue(), 20000);
                        if (genes == null)
                            continue;
                        hasCandidateGeneLists = true;
                        BoolQueryBuilder rangeOrFilter = QueryBuilders.boolQuery();
                        for (Gene gene : genes) {
                            rangeOrFilter.should(QueryBuilders.boolQuery()
                                    .filter(QueryBuilders.termQuery("chr", gene.getChr()))
                                    .filter(QueryBuilders.rangeQuery("position").from(gene.getStart()).to(gene.getEnd()))
                            );
                        }
                        itemFilterBuilder.should(rangeOrFilter);
                    }
                }
                if (!hasCandidateGeneLists)
                    itemFilterBuilder = null;
                break;

        }

        return itemFilterBuilder;
    }

    private QueryBuilder getQueryBuilder(QueryBuilder filter, String parentType) {
        if (parentType != null) {
            return QueryBuilders.hasParentQuery(parentType, filter);
        }
        return filter;
    }

    //TODO optimize because getChr() of Gene does substr
    private List<Gene> getCandidateGeneListRanges(String id, int additonalRange) {
        GetRequestBuilder request = client.prepareGet(esAclManager.getIndex(), "candidate_gene_list", id).setFields("genes.start_pos", "genes.end_pos", "genes.chr");
        GetResponse response = request.execute().actionGet();
        if (!response.isExists())
            return null;
        List<Gene> ranges = Lists.newArrayList();
        GetField genesStartPos = response.getField("genes.start_pos");
        GetField genesEndPos = response.getField("genes.end_pos");
        GetField genesChr = response.getField("genes.chr");
        for (int i = 0; i < genesStartPos.getValues().size(); i++) {
            Integer startPos = (Integer) genesStartPos.getValues().get(i) - additonalRange;
            Integer endPos = (Integer) genesEndPos.getValues().get(i) + additonalRange;
            String chr = (String) genesChr.getValue();
            ranges.add(new Gene(startPos, endPos, 0, "AT" + chr, null));
        }
        return ranges;
    }

    private List<SNPAnnotation> convertOldAnnotationTonewFormat(String annotationString) {
        List<SNPAnnotation> annotations = Lists.newArrayList();
        SNPAnnotation annotation = null;
        if (annotationString != null) {
            switch (annotationString) {
                case "S":
                    annotation = new SNPAnnotation("S");
                    break;
                case "NS":
                    annotation = new SNPAnnotation("NS");
                    break;
                default:
                    annotation = new SNPAnnotation("*");
            }
        }
        if (annotation != null) {
            annotations.add(annotation);
        }
        return annotations;
    }


    @Override
    public MetaAnalysisPage findTopAnalysis(MetaAnalysisTopResultsCriteria criteria, List<FilterItem> filterItems, int start, int size) {
        List<MetaAnalysis> metaAnalyses = Lists.newArrayList();
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());

        QueryBuilder filter = getFilterFromCriteria(criteria, filterItems);
        if (filter == null) {
            builder.setQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.setQuery(QueryBuilders.constantScoreQuery(filter));
        }
        builder.setSize(size).setFrom(start).addSort("score", SortOrder.DESC).addFields("position", "mac", "maf", "chr", "score", "overFDR", "studyid", "_parent", "annotations.gene_name", "annotation", "inGene", "_parent").setTypes("meta_analysis_snps");
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
                MetaAnalysis.Builder metaAnalysisBuilder = MetaAnalysis.builder()
                        .setAnalysisId(studyId)
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId())
                        .setAssociations(ImmutableList.of(getAssociation(fields, true, null)));
                metaAnalyses.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                logger.warn("Failed to retrieve top associations", e);
            }
        }
        return new MetaAnalysisPage(metaAnalyses, new PageRequest(start / size, size), response.getHits().getTotalHits(), 0);
    }

    @Override
    public CandidateGeneListPage findCandidateGeneLists(ConstEnums.TABLE_FILTER filter, String searchString, int page, int size) {
        SearchResponse response = esSearcher.search(filter, false, new String[]{"name^3.5", "name.partial^1.5", "description"}, searchString, CandidateGeneList.ES_TYPE, page, size);
        List<Long> idsToFetch = EsSearcher.getIdsFromResponse(response);
        List<CandidateGeneList> resultsFromDb = candidateGeneListRepository.findAll(idsToFetch);
        //extract facets
        List<ESFacet> facets = EsSearcher.getAggregations(response);
        Ordering<CandidateGeneList> orderByEs = Ordering.explicit(idsToFetch).onResultOf(DomainFunctions.getCandidateGeneListId());
        List<CandidateGeneList> results = orderByEs.immutableSortedCopy(resultsFromDb);
        aclManager.setPermissionAndOwners(results);
        return new CandidateGeneListPage(results, new PageRequest(page, size), response.getHits().getTotalHits(), facets);
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
            esIndexer.index(candidateGeneList, true);
        } catch (IOException e) {
            logger.error("Failed to index candidate gene list", e);
        }
    }



    @Transactional(readOnly = false)
    @Override
    public void deleteCandidateGeneList(CandidateGeneList candidateGeneList) {
        aclManager.setPermissionAndOwner(candidateGeneList);
        //check not public
        if (candidateGeneList.isPublic()) {
            throw new RuntimeException("Public candidateGeneLists can't be deleted");
        }
        for (CandidateGeneListEnrichment enrichment : candidateGeneList.getCandidateGeneListEnrichments()) {
            enrichment.delete();
        }
        candidateGeneListRepository.delete(candidateGeneList);
        aclManager.deletePermissions(candidateGeneList, true);
        deleteCandidateGeneListFromIndex(candidateGeneList);
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
        request.setSize(size).setFrom(page).setTypes("candidate_gene_list").setFetchSource("genes", null);
        QueryBuilder idQuery = QueryBuilders.idsQuery().addIds(candidateGeneList.getId().toString());

        if (searchString != null) {
            QueryInnerHitBuilder queryInnerHitBuilder = new QueryInnerHitBuilder().setSize(1)
                    .setFetchSource(true);
            request.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.nestedQuery("genes", QueryBuilders.termQuery("genes.name", searchString)).innerHit(queryInnerHitBuilder)).filter(idQuery));
        } else {
            request.setQuery(QueryBuilders.constantScoreQuery(idQuery));
        }
        NestedBuilder aggrs = AggregationBuilders.nested("genes").path("genes");
        aggrs.subAggregation(AggregationBuilders.terms("annotation").field("genes.annotation").size(5))
                .subAggregation(AggregationBuilders.terms("chr").field("genes.chr").size(5))
                .subAggregation(AggregationBuilders.terms("strand").field("genes.strand").size(2));
        request.addAggregation(aggrs);

        SearchResponse response = request.execute().actionGet();
        if (response.getHits().getTotalHits() > 0) {
            SearchHit hit = response.getHits().getAt(0);
            if (searchString == null) {
                genes = extractGeneInfos(hit);
            } else {
                genes = Lists.newArrayList(extractGeneInfo(hit.getInnerHits().get("genes").getAt(0).getSource()));
            }
        }
        List<ESFacet> statsFacets = Lists.newArrayList();
        List<ESTermsFacet> terms = Lists.newArrayList();
        Nested searchFacets = response.getAggregations().get("genes");
        List<ESFacet> facets = Lists.newArrayList();
        facets.add(new ESFacet(ConstEnums.GENE_FILTER.ALL.name(), 0, genes.size(), 0, null));
        Terms annotationFacet = searchFacets.getAggregations().get("annotation");
        //annotation
        for (Terms.Bucket termEntry : annotationFacet.getBuckets()) {
            ConstEnums.GENE_FILTER annotFilter = getFilterFromFacet(termEntry.getKeyAsString());
            if (annotFilter != null) {
                facets.add(new ESFacet(annotFilter.name(), 0, termEntry.getDocCount(), 0, null));
            }
            terms.add(new ESTermsFacet(termEntry.getKeyAsString(), termEntry.getDocCount()));
        }
        statsFacets.add(new ESFacet("annotation", 0, terms.size(), 0, terms));
        //chr

        terms = Lists.newArrayList();

        Terms statsTermFacet = searchFacets.getAggregations().get("chr");
        for (Terms.Bucket termEntry : statsTermFacet.getBuckets()) {
            terms.add(new ESTermsFacet(String.format("Chr%s", termEntry.getKey()), termEntry.getDocCount()));
        }
        statsFacets.add(new ESFacet("chr", 0, terms.size(), 0, terms));
        //strand
        terms = Lists.newArrayList();
        statsTermFacet = searchFacets.getAggregations().get("strand");
        for (Terms.Bucket termEntry : statsTermFacet.getBuckets()) {
            terms.add(new ESTermsFacet(termEntry.getKeyAsString(), termEntry.getDocCount()));
        }
        statsFacets.add(new ESFacet("strand", 0, terms.size(), 0, terms));

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
            // If Enrichments exists remove them.
            checkForEnrichment(candidateGeneList);
            candidateGeneListRepository.save(candidateGeneList);
            candidateGeneList.getGenesWithInfo().add(gene);
            indexCandidateGeneList(candidateGeneList);

        }
        return gene;
    }

    private void checkForEnrichment(CandidateGeneList list) {
        if (list.getEnrichmentCount() > 0) {
            for (CandidateGeneListEnrichment enrichment : list.getCandidateGeneListEnrichments()) {
                enrichment.delete();
            }
            list.getCandidateGeneListEnrichments().clear();
            deleteCandidateGeneListEnrichmentsFromIndex(list.getId());
        }
    }

    @Transactional(readOnly = false)
    @Override
    public List<Gene> addGenesToCandidateGeneList(Long id, List<String> geneIds) {
        CandidateGeneList candidateGeneList = candidateGeneListRepository.findOne(id);
        if (candidateGeneList == null)
            return null;
        candidateGeneList.setGenesWithInfo(getGeneInfos(candidateGeneList));
        List<Gene> genes = Lists.newArrayList();
        for (String geneId : geneIds) {
            Gene gene = annotationService.getGeneById(geneId);
            if (gene != null && !candidateGeneList.getGenes().contains(geneId)) {
                genes.add(gene);
                candidateGeneList.getGenes().add(geneId);
                candidateGeneList.getGenesWithInfo().add(gene);
            }
        }
        checkForEnrichment(candidateGeneList);
        candidateGeneListRepository.save(candidateGeneList);
        indexCandidateGeneList(candidateGeneList);
        return genes;
    }

    @Transactional(readOnly = false)
    @Override
    public void removeGeneFromCandidateGeneList(CandidateGeneList candidateGeneList, String geneId) {
        candidateGeneList.getGenes().remove(geneId);
        checkForEnrichment(candidateGeneList);
        candidateGeneListRepository.save(candidateGeneList);
        candidateGeneList.setGenesWithInfo(getGeneInfos(candidateGeneList));
        candidateGeneList.getGenesWithInfo().remove(new Gene(0, 0, 0, geneId, null));
        indexCandidateGeneList(candidateGeneList);
    }

    @Override
    public List<Gene> getGenesInCandidateGeneListEnrichment(Long id) {
        CandidateGeneList list = candidateGeneListRepository.findOne(id);
        //TODO optimize doesn't need all fields only start and end
        List<Gene> genes = getGeneInfos(list);
        return genes;
    }

    private boolean isCandidateGeneListInStudy(Study study, Long candidateGeneListId) {
        for (CandidateGeneListEnrichment enrichment : study.getCandidateGeneListEnrichments()) {
            if (enrichment.getCandidateGeneList() != null && candidateGeneListId.equals(enrichment.getCandidateGeneList().getId())) {
                return true;
            }
        }
        return false;
    }


    private CandidateGeneListEnrichmentPage findAvailableCandidateGeneListEnrichments(final Long id, ConstEnums.ENRICHMENT_TYPE type, String searchString, int page, int size) {
        if (type == ConstEnums.ENRICHMENT_TYPE.CANDIDATE_GENE_LIST) {
            SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
            request.setSize(size).setFrom(page).setTypes("study").setNoFields();
            QueryBuilder aclFilter = getAclFilterForEnrichment(type, false);
            QueryBuilder entityFilter = getEntityFilterForEnrichment(type, id);
            QueryBuilder availableFilter = QueryBuilders.boolQuery()
                    .mustNot(QueryBuilders.hasChildQuery("candidate_gene_list_enrichment", entityFilter));
            QueryBuilder searchFilter = QueryBuilders.boolQuery().filter(aclFilter).filter(availableFilter);

            QueryBuilder query = QueryBuilders.matchAllQuery();
            if (searchString != null && !searchString.equalsIgnoreCase("")) {
                query = multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "genotype.name^1.5", "genotype.producer", "owner.name", "experiment.name", "phenotype.name");
            }
            request.setQuery(boolQuery().must(query).filter(searchFilter));
            SearchResponse response = request.execute().actionGet();
            Set<Long> idsToFetch = ImmutableSet.copyOf(EsSearcher.getIdsFromResponse(response));
            List<Study> studiesFromDb = studyRepository.findAll(idsToFetch);
            Ordering<Study> orderByEs = Ordering.explicit(ImmutableList.copyOf(idsToFetch)).onResultOf(DomainFunctions.getStudyId());
            List<Study> results = orderByEs.immutableSortedCopy(studiesFromDb);

            List<CandidateGeneListEnrichment> enrichments = Lists.newArrayList(Iterables.filter(Lists.transform(results, new Function<Study, CandidateGeneListEnrichment>() {
                @Nullable
                @Override
                public CandidateGeneListEnrichment apply(@Nullable Study study) {
                    if (study != null && !isCandidateGeneListInStudy(study, id)) {
                        CandidateGeneListEnrichment enrichment = new CandidateGeneListEnrichment();
                        enrichment.setStudy(study);
                        return enrichment;
                    }
                    return null;
                }
            }), Predicates.notNull()));
            return new CandidateGeneListEnrichmentPage(enrichments, new PageRequest(page, size), (enrichments.size() == 0 ? 0 : response.getHits().getTotalHits()), null);
        } else if (type == ConstEnums.ENRICHMENT_TYPE.ANALYSIS) {
            List<Long> ids = candidateGeneListRepository.findExistingEnrichmentByStudy(id);
            SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
            request.setTypes("candidate_gene_list").setNoFields().setSize(size).setFrom(page);
            QueryBuilder query = QueryBuilders.matchAllQuery();
            if (searchString != null && !searchString.equalsIgnoreCase("")) {
                query = multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "genotype.name^1.5", "genotype.producer", "owner.name", "experiment.name", "phenotype.name");
            }
            request.setQuery(QueryBuilders.boolQuery()
                    .must(query)
                    .filter(esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")))
                    .mustNot(QueryBuilders.idsQuery().addIds(Lists.transform(ids, new Function<Long, String>() {
                        @Nullable
                        @Override
                        public String apply(@Nullable Long input) {
                            return String.valueOf(input);
                        }
                    }).toArray(new String[]{}))));
            SearchResponse response = request.execute().actionGet();
            ImmutableSet<Long> idsToFetch = ImmutableSet.copyOf(EsSearcher.getIdsFromResponse(response));

            List<CandidateGeneList> candidateGeneLists = candidateGeneListRepository.findAll(idsToFetch);
            Ordering<CandidateGeneList> orderByEs = Ordering.explicit(ImmutableList.copyOf(idsToFetch)).onResultOf(DomainFunctions.getCandidateGeneListId());
            List<CandidateGeneList> results = orderByEs.immutableSortedCopy(candidateGeneLists);
            final Study entity = studyRepository.findOne(id);
            List<CandidateGeneListEnrichment> enrichments = Lists.newArrayList(Iterables.filter(Iterables.transform(results, new Function<CandidateGeneList, CandidateGeneListEnrichment>() {
                @Nullable
                @Override
                public CandidateGeneListEnrichment apply(@Nullable CandidateGeneList candidateGeneList) {
                    if (candidateGeneList != null) {
                        CandidateGeneListEnrichment enrichment = new CandidateGeneListEnrichment();
                        enrichment.setStudy(entity);
                        enrichment.setCandidateGeneList(candidateGeneList);
                        return enrichment;
                    }
                    return null;
                }
            }), Predicates.notNull()));
            return new CandidateGeneListEnrichmentPage(enrichments, new PageRequest(page, size), (enrichments.size() == 0 ? 0 : response.getHits().getTotalHits()), null);
        }
        return null;
    }


    @Override
    public CandidateGeneListEnrichmentPage findCandidateGeneListEnrichments(Long id, ConstEnums.ENRICHMENT_TYPE type, ConstEnums.ENRICHMENT_FILTER currentFilter, String searchString, int page, int size) {

        if (currentFilter == ConstEnums.ENRICHMENT_FILTER.AVAILABLE) {
            return findAvailableCandidateGeneListEnrichments(id, type, searchString, page, size);
        }
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(page).setTypes("candidate_gene_list_enrichment").setNoFields();
        QueryBuilder aclFilter = getAclFilterForEnrichment(type, true);
        QueryBuilder finishedFilter = QueryBuilders.termQuery("status", "Finished");
        QueryBuilder runningFilter = QueryBuilders.termsQuery("status", "Running", "Waiting", "Failed");
        QueryBuilder entityFilter = getEntityFilterForEnrichment(type, id);

        QueryBuilder typeFilter = null;
        switch (currentFilter) {
            case FINISHED:
                typeFilter = finishedFilter;
                request.addAggregation(AggregationBuilders.stats("maxpvalue").field("pvalue"));
                request.addSort("pvalue", SortOrder.ASC);
                break;

            case RUNNING:
                typeFilter = runningFilter;
                break;
        }
        QueryBuilder searchFilter = QueryBuilders.boolQuery().filter(aclFilter).filter(entityFilter).filter(typeFilter);

        QueryBuilder query = matchAllQuery();
        String[] fieldsToSearch = new String[]{"candidategenelist.name", "experiment_.name", "phenotype_.name", "study_.name"};
        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            query = multiMatchQuery(searchString, fieldsToSearch);
        }
        request.setQuery(filteredQuery(query, searchFilter));

        SearchResponse response = request.execute().actionGet();
        //required because of possible duplicates when routing is wrongly assigned
        Set<Long> idsToFetch = ImmutableSet.copyOf(EsSearcher.getIdsFromResponse(response));
        List<CandidateGeneListEnrichment> resultsFromDb = candidateGeneListEnrichmentsRepository.findAll(idsToFetch);
        Ordering<CandidateGeneListEnrichment> orderByEs = Ordering.explicit(ImmutableList.copyOf(idsToFetch)).onResultOf(DomainFunctions.getCandidateGeneListEnrichmentId());
        List<CandidateGeneListEnrichment> results = orderByEs.immutableSortedCopy(resultsFromDb);

        List<ESFacet> facets = Lists.newArrayList();
        if (currentFilter == ConstEnums.ENRICHMENT_FILTER.FINISHED) {
            Aggregations aggregations = response.getAggregations();
            Stats stats = aggregations.get("maxpvalue");
            double maxPvalue = 0;
            if (!Double.isInfinite(stats.getMax()))
                maxPvalue = stats.getMax();
            ESTermsFacet term = new ESTermsFacet("maxpvalue", maxPvalue);

            facets.add(new ESFacet("maxpvalue", 0, 0, 0, Lists.newArrayList(term)));
        }

        return new CandidateGeneListEnrichmentPage(results, new PageRequest(page, size), response.getHits().getTotalHits(), facets);
    }


    @Override
    public List<ESFacet> findEnrichmentStats(Long id, ConstEnums.ENRICHMENT_TYPE type, String searchString) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());

        request.setNoFields().setSize(0);
        QueryBuilder typeFilter = getEntityFilterForEnrichment(type, id);
        QueryBuilder aclFilter;
        QueryBuilder availableFilter = null;
        QueryBuilder runningFilter = QueryBuilders.boolQuery().filter(typeFilter).filter(QueryBuilders.termsQuery("status", "Running", "Waiting", "Failed"));
        QueryBuilder finishedFilter = QueryBuilders.boolQuery().filter(typeFilter).filter(QueryBuilders.termsQuery("status", "Finished"));
        if (type == ConstEnums.ENRICHMENT_TYPE.CANDIDATE_GENE_LIST) {
            request.setTypes("study");
            aclFilter = getAclFilterForEnrichment(type, false);
            availableFilter = QueryBuilders.boolQuery().mustNot(QueryBuilders.hasChildQuery("candidate_gene_list_enrichment", typeFilter));
            finishedFilter = QueryBuilders.hasChildQuery("candidate_gene_list_enrichment", finishedFilter);
            runningFilter = QueryBuilders.hasChildQuery("candidate_gene_list_enrichment", runningFilter);
        } else {
            request.setTypes("candidate_gene_list_enrichment");
            aclFilter = getAclFilterForEnrichment(type, true);
        }
        request.addAggregation(AggregationBuilders.filter(ConstEnums.ENRICHMENT_FILTER.FINISHED.name()).filter(finishedFilter));
        request.addAggregation(AggregationBuilders.filter(ConstEnums.ENRICHMENT_FILTER.RUNNING.name()).filter(runningFilter));

        if (availableFilter != null) {
            request.addAggregation(AggregationBuilders.filter(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name()).filter(availableFilter));
        }

        QueryBuilder query = QueryBuilders.matchAllQuery();
        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            query = multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "genotype.name^1.5", "genotype.producer", "owner.name", "experiment.name", "phenotype.name");
        }
        request.setQuery(QueryBuilders.filteredQuery(query, aclFilter));
        SearchResponse response = request.execute().actionGet();


        Aggregations aggregations = response.getAggregations();

        List<ESFacet> facets = Lists.newArrayList();

        Filter filterFacet = aggregations.get(ConstEnums.ENRICHMENT_FILTER.FINISHED.name());
        facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.FINISHED.name(), 0, filterFacet.getDocCount(), 0, null));
        filterFacet = aggregations.get(ConstEnums.ENRICHMENT_FILTER.RUNNING.name());
        facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.RUNNING.name(), 0, filterFacet.getDocCount(), 0, null));

        if (type == ConstEnums.ENRICHMENT_TYPE.CANDIDATE_GENE_LIST) {
            filterFacet = aggregations.get(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name());
            facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name(), 0, filterFacet.getDocCount(), 0, null));
        } else if (type == ConstEnums.ENRICHMENT_TYPE.ANALYSIS) {
            long count = findAvailableCandidateGeneListEnrichmentsForStudyCount(id);
            facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name(), 0, count, 0, null));
        }
        return facets;
    }


    private long findAvailableCandidateGeneListEnrichmentsForStudyCount(Long studyId) {
        List<Long> ids = candidateGeneListRepository.findExistingEnrichmentByStudy(studyId);
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setTypes("candidate_gene_list").setNoFields().setSize(0);
        request.setQuery(QueryBuilders.boolQuery()
                .filter(esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")))
                .mustNot(QueryBuilders.idsQuery().addIds(Lists.transform(ids, new Function<Long, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Long input) {
                        return String.valueOf(input);
                    }
                }).toArray(new String[]{}))));
        SearchResponse response = request.execute().actionGet();
        return response.getHits().getTotalHits();
    }

    private QueryBuilder getAclFilterForEnrichment(ConstEnums.ENRICHMENT_TYPE type, boolean isInEnrichment) {
        List<String> permissions = Lists.newArrayList("read");
        QueryBuilder studyAclFilter = esAclManager.getAclFilterForPermissions(permissions);
        QueryBuilder aclFilter = (isInEnrichment ? QueryBuilders.hasParentQuery("study", studyAclFilter) : studyAclFilter);

        if (!(type == ConstEnums.ENRICHMENT_TYPE.CANDIDATE_GENE_LIST)) {
            QueryBuilder candidateAcl = esAclManager.getAclFilterForPermissions(permissions, "candidate_gene_list_acl");
            aclFilter = QueryBuilders.boolQuery()
                    .filter(aclFilter)
                    .filter(isInEnrichment ? candidateAcl : QueryBuilders.hasChildQuery("candidate_gene_list_enrichment", candidateAcl));
        }
        return aclFilter;
    }


    private QueryBuilder getEntityFilterForEnrichment(ConstEnums.ENRICHMENT_TYPE type, Long id) {
        QueryBuilder typeFilter = null;
        //TODO change to visitor or strategy pattern
        switch (type) {
            case CANDIDATE_GENE_LIST:
                typeFilter = QueryBuilders.termQuery("candidategenelist.id", id.toString());
                break;
            case STUDY:
                typeFilter = QueryBuilders.termQuery("experiment_.id", id.toString());
                break;
            case PHENOTYPE:
                typeFilter = QueryBuilders.termQuery("phenotype_.id", id.toString());
                break;
            case ANALYSIS:
                typeFilter = QueryBuilders.termQuery("study_.id", id.toString());
                break;
            default:
                throw new RuntimeException("entity unknown");
        }
        return typeFilter;
    }

    @Override
    @Transactional(readOnly = false)
    public void createCandidateGeneListEnrichments(Long id, ConstEnums.ENRICHMENT_TYPE type, boolean isAllChecked, List<CandidateGeneListEnrichment> candidateGeneListEnrichments) {
        if (isAllChecked) {
            //TODO switch to scroll API because otherwise causes exception
            candidateGeneListEnrichments = getCandidateGeneEnrichmentFromEntity(id, type);
        }
        Iterable<CandidateGeneListEnrichment> filteredRecords = Iterables.filter(candidateGeneListEnrichments, new Predicate<CandidateGeneListEnrichment>() {
            @Override
            public boolean apply(CandidateGeneListEnrichment candidateGeneListEnrichment) {
                Preconditions.checkNotNull(candidateGeneListEnrichment);
                return candidateGeneListEnrichment.getId() == null;
            }
        });
        SecureEntity entity = getEnrichmentEntityFromIdAndType(id, type);
        for (CandidateGeneListEnrichment enrichment : filteredRecords) {
            if (type == ConstEnums.ENRICHMENT_TYPE.CANDIDATE_GENE_LIST) {
                enrichment.setCandidateGeneList((CandidateGeneList) entity);
            } else if (type == ConstEnums.ENRICHMENT_TYPE.ANALYSIS) {
                enrichment.setStudy((Study) entity);
            }
            enrichment.setStatus("Waiting");
            enrichment.setTopSNPCount(1000);
            enrichment.setPermutationCount(10000);
            enrichment.setProgress(0);
            enrichment.setWindowsize(20000);
        }
        candidateGeneListEnrichmentsRepository.save(candidateGeneListEnrichments);
        indexCandidateGeneListEnrichments(candidateGeneListEnrichments);
    }

    private SecureEntity getEnrichmentEntityFromIdAndType(Long id, ConstEnums.ENRICHMENT_TYPE type) {
        switch (type) {
            case CANDIDATE_GENE_LIST:
                return candidateGeneListRepository.findOne(id);
            case ANALYSIS:
                return studyRepository.findOne(id);
            default:
                throw new RuntimeException(String.format("Type %s not supported", type));
        }
    }


    private List<CandidateGeneListEnrichment> getCandidateGeneEnrichmentFromEntity(Long id, ConstEnums.ENRICHMENT_TYPE type) {
        long count = 0;
        if (type == ConstEnums.ENRICHMENT_TYPE.CANDIDATE_GENE_LIST) {
            count = studyRepository.count() * 10;
        } else {
            count = candidateGeneListRepository.count() * 10;
        }
        CandidateGeneListEnrichmentPage page = findCandidateGeneListEnrichments(id, type, ConstEnums.ENRICHMENT_FILTER.AVAILABLE, "", 0, (int) count);
        return page.getContents();
    }

    //TODO switch to use ESIndexer Class
    private void indexCandidateGeneListEnrichments(List<CandidateGeneListEnrichment> candidateGeneListEnrichments) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        //necesarry to improve performance
        ImmutableSet<CandidateGeneList> candidateGeneLists = ImmutableSet.copyOf(Iterables.transform(candidateGeneListEnrichments, new Function<CandidateGeneListEnrichment, CandidateGeneList>() {
            @Override
            public CandidateGeneList apply(CandidateGeneListEnrichment candidateGeneListEnrichment) {
                Preconditions.checkNotNull(candidateGeneListEnrichment);
                return candidateGeneListEnrichment.getCandidateGeneList();
            }
        }));
        Map<ObjectIdentity, Acl> acls = aclManager.getAcls(candidateGeneLists);
        Map<ObjectIdentity, List<Map<String, Object>>> aclBuilders = Maps.transformValues(acls, new Function<Acl, List<Map<String, Object>>>() {
            @Nullable
            @Override
            public List<Map<String, Object>> apply(@Nullable Acl acl) {
                return esAclManager.getACLContent(null, acl);
            }
        });
        for (CandidateGeneListEnrichment enrichment : candidateGeneListEnrichments) {
            XContentBuilder builder = getBuilderForCandidateGeneListEnrichment(enrichment, aclBuilders.get(new ObjectIdentityImpl(enrichment.getCandidateGeneList())));
            if (builder != null) {
                bulkRequest.add(client.prepareIndex(esAclManager.getIndex(), "candidate_gene_list_enrichment", enrichment.getId().toString())
                        .setParent(enrichment.getStudy().getId().toString())
                        .setRouting(enrichment.getStudy().getPhenotype().getExperiment().getId().toString()) // required bcause otherwise routing exception even if path is mapped
                        .setSource(builder)
                );
            }
        }
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
    }


    public void indexCandidateGeneListEnrichment(CandidateGeneListEnrichment enrichment) {
        indexCandidateGeneListEnrichments(Lists.newArrayList(enrichment));
    }

    private XContentBuilder getBuilderForCandidateGeneListEnrichment(CandidateGeneListEnrichment enrichment, List<Map<String, Object>> acls) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject()
                    .field("modified", enrichment.getModified())
                    .field("created", enrichment.getCreated())
                    .field("status", enrichment.getStatus())
                    .field("progress", enrichment.getProgress())
                    .field("pvalue", enrichment.getPvalue())
                    .field("windowsize", enrichment.getWindowsize())
                    .field("permutationcount", enrichment.getPermutationCount())
                    .field("top_snps_count", enrichment.getTopSNPCount())
                    .startObject("candidategenelist")
                    .field("name", enrichment.getCandidateGeneList().getName())
                    .field("id", enrichment.getCandidateGeneList().getId())
                    .endObject()
                    .startObject("experiment_")
                    .field("name", enrichment.getStudy().getPhenotype().getExperiment().getName())
                    .field("id", enrichment.getStudy().getPhenotype().getExperiment().getId())
                    .endObject()
                    .startObject("phenotype_")
                    .field("name", enrichment.getStudy().getPhenotype().getLocalTraitName())
                    .field("id", enrichment.getStudy().getPhenotype().getId())
                    .endObject()
                    .startObject("study_")
                    .field("name", enrichment.getStudy().getName())
                    .field("id", enrichment.getStudy().getId())
                    .endObject();

            builder.array("candidate_gene_list_acl", acls);
            builder.endObject();
            return builder;
        } catch (IOException e) {
            logger.error("Failed to create index document for candidate gene list enrichment", e);
        }
        return null;
    }


    private void deleteCandidateGeneListFromIndex(CandidateGeneList candidateGeneList) {
        esIndexer.delete(candidateGeneList);
        deleteCandidateGeneListEnrichmentsFromIndex(candidateGeneList.getId());
    }

    private void deleteCandidateGeneListEnrichmentsFromIndex(Long candidateGeneListId) {
        new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE)
                .setIndices(esAclManager.getIndex())
                .setTypes("candidate_gene_list_enrichment")
                .setQuery(QueryBuilders.termQuery("candidategenelist.id", candidateGeneListId))
                .execute();
    }

    private Gene extractGeneInfo(Map<String, Object> field) {
        Gene gene = new Gene((long) (Integer) field.get("start_pos"), (long) (Integer) field.get("end_pos"), (Integer) field.get("strand"), (String) field.get("name"), null);
        gene.setAnnotation((String) field.get("annotation"));
        gene.setCuratorSummary((String) field.get("curator_summary"));
        gene.setSynonyms((List<String>) field.get("synonyms"));
        gene.setDescription((String) field.get("desdeletecription"));
        gene.setShortDescription((String) field.get("short_description"));
        if (field.containsKey("GO")) {
            List<Object> goTerms = (List<Object>) field.get("GO");
            for (Object goTermItem : goTerms) {
                Map<String, Object> goTermFields = (Map<String, Object>) goTermItem;
                gene.getGoTerms().add(new GoTerm((String) goTermFields.get("relation"), (String) goTermFields.get("exact"), (String) goTermFields.get("narrow")));
            }
        }
        return gene;
    }

    private List<Gene> extractGeneInfos(SearchHit hit) {
        List<Gene> genes = Lists.newArrayList();
        final Map<String, Object> source = hit.getSource();
        if (source != null && !source.isEmpty()) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) source.get("genes");
            for (Map<String, Object> field : items) {

                genes.add(extractGeneInfo(field));
            }
        }
        return genes;
    }


    private List<Gene> getGeneInfos(CandidateGeneList candidateGeneList) {
        List<Gene> genes = Lists.newArrayList();
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setTypes("candidate_gene_list").setFetchSource("genes", null);
        request.setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.idsQuery().addIds(candidateGeneList.getId().toString())));
        SearchResponse response = request.execute().actionGet();
        if (response.getHits().getTotalHits() > 0) {
            genes = extractGeneInfos(response.getHits().getAt(0));
        }
        return genes;
    }

    @Override
    public String getClassNameFromEnrichmentType(ConstEnums.ENRICHMENT_TYPE type) {
        switch (type) {
            case CANDIDATE_GENE_LIST:
                return CandidateGeneList.class.getName();
            case ANALYSIS:
                return Study.class.getName();
            case PHENOTYPE:
                return TraitUom.class.getName();
            case STUDY:
                return Experiment.class.getName();
        }
        throw new RuntimeException(String.format("Type %s not supported", type));
    }
}

