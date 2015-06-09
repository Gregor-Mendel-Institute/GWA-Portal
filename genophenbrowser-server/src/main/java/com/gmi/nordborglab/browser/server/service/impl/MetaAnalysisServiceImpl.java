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
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria;
import com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListEnrichmentPage;
import com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListPage;
import com.gmi.nordborglab.browser.server.domain.pages.GenePage;
import com.gmi.nordborglab.browser.server.domain.pages.MetaSNPAnalysisPage;
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
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.sort.SortOrder;
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

import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
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

    private static enum ENRICHMENT_VIEW_TYPE {CANDIDATE_GENE_LIST, EXPERIMENT, PHENTOYPE, STUDY}

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

    @Override
    public MetaSNPAnalysisPage findAllAnalysisForRegion(int startPos, int endPos, String chr, int start, int size, List<FilterItem> filterItems) {
        List<MetaSNPAnalysis> metaSNPAnalysises = Lists.newArrayList();
        // GET all studyids
        // TODO remove this check
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        BoolFilterBuilder filter = FilterBuilders.boolFilter().must(
                FilterBuilders.hasChildFilter("meta_analysis_snps", FilterBuilders.boolFilter().
                        must(
                                FilterBuilders.numericRangeFilter("position").from(startPos).to(endPos),
                                FilterBuilders.termFilter("chr", chr)
                        )
                ),
                esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")));
        builder.addFields("_id", "_parent").setTypes("study").setQuery(QueryBuilders.constantScoreQuery(filter)).setSize(Integer.MAX_VALUE);
        SearchResponse response = builder.execute().actionGet();
        List<Long> ids = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            ids.add(Long.parseLong(hit.getId()));
        }
        if (ids.size() == 0) {
            return new MetaSNPAnalysisPage(metaSNPAnalysises, new PageRequest(start / size, size), 0);
        }
        Iterable<Study> studies = studyRepository.findAll(ids);
        Map<Long, Study> studyCache = Maps.uniqueIndex(studies, new Function<Study, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable Study study) {
                return study.getId();
            }
        });

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
                        , FilterBuilders.numericRangeFilter("position").from(startPos).to(endPos),
                        FilterBuilders.termFilter("chr", chr));
        FilterBuilder filterItemFilter = getFilterFromFilterItems(filterItems);
        if (filterItemFilter != null) {
            filter.must(filterItemFilter);
        }
        builder.setSize(size).setFrom(start).addSort("score", SortOrder.DESC).addFields("position", "mac", "maf", "_parent", "score", "overFDR", "studyid", "annotation", "inGene").setTypes("meta_analysis_snps").setQuery(QueryBuilders.constantScoreQuery(filter));
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
                SNPInfo info = new SNPInfo();
                info.setPosition((Integer) fields.get("position").getValue());
                info.setChr(chr);
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
                MetaSNPAnalysis.Builder metaAnalysisBuilder = new MetaSNPAnalysis.Builder()
                        .setAnalysisId(studyId)
                        .setSnpAnnotation(info)
                        .setpValue((Double) fields.get("score").getValue())
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setOverFDR((Boolean) fields.get("overFDR").getValue())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId());
                if (fields.containsKey("mac")) {
                    metaAnalysisBuilder.setMac((Integer) fields.get("mac").getValue());
                }
                if (fields.containsKey("maf")) {
                    metaAnalysisBuilder.setMaf((Double) fields.get("maf").getValue());
                }
                metaSNPAnalysises.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new MetaSNPAnalysisPage(metaSNPAnalysises, new PageRequest(start / size, size), response.getHits().getTotalHits());
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
        FilterBuilder filter = getFilterFromCriteria(criteria, filterItems);
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
            if (rangeEntry.getFrom() == Double.NEGATIVE_INFINITY) {
                range = "< " + rangeEntry.getTo().doubleValue() * 100 + "%";
            } else if (rangeEntry.getTo() == Double.POSITIVE_INFINITY) {
                range = "> " + rangeEntry.getFrom().doubleValue() * 100 + "%";
            } else {
                range = String.format("%s - %s", rangeEntry.getFrom().doubleValue() * 100, rangeEntry.getTo().doubleValue() * 100 + "%");
            }
            terms.add(new ESTermsFacet(range, rangeEntry.getDocCount()));
        }
        facets.add(new ESFacet("maf", 0, 0, 0, terms));


        // get chr facet
        Terms searchFacet = aggregations.get("chr");
        terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : searchFacet.getBuckets()) {
            terms.add(new ESTermsFacet(String.format("Chr%s", termEntry.getKeyAsText().string()), termEntry.getDocCount()));
        }
        facets.add(new ESFacet("chr", 0, 0, 0, terms));

        // get inGene facet
        searchFacet = aggregations.get("inGene");
        terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : searchFacet.getBuckets()) {
            String term = "intergenic";
            if (termEntry.getKeyAsText().string().equalsIgnoreCase("T")) {
                term = "genic";
            }
            terms.add(new ESTermsFacet(term, termEntry.getDocCount()));
        }
        facets.add(new ESFacet("inGene", 0, 0, 0, terms));

        // get annotation
        searchFacet = aggregations.get("annotation");
        terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : searchFacet.getBuckets()) {
            terms.add(new ESTermsFacet(termEntry.getKeyAsText().string(), termEntry.getDocCount()));
        }
        facets.add(new ESFacet("annotation", 0, 0, 0, terms));

        return facets;
    }

    //TODO optimize filter order (bool vs and)   http://www.elasticsearch.org/blog/all-about-elasticsearch-filter-bitsets/
    private FilterBuilder getFilterFromCriteria(MetaAnalysisTopResultsCriteria criteria, List<FilterItem> filterItems) {
        AndFilterBuilder filter = FilterBuilders.andFilter();
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        boolFilter.must(FilterBuilders.hasParentFilter("study", esAclManager.getAclFilterForPermissions(Lists.newArrayList("read"))));
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
                RangeFilterBuilder mafFilter = FilterBuilders.rangeFilter("maf");
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

        FilterBuilder filterItemsFilter = getFilterFromFilterItems(filterItems);
        if (filterItemsFilter != null) {
            filter.add(filterItemsFilter);
        }

        return filter;

    }


    private FilterBuilder getFilterFromFilterItems(List<FilterItem> filterItems) {
        if (filterItems != null && filterItems.size() > 0) {
            AndFilterBuilder andFilter = FilterBuilders.andFilter();
            for (FilterItem filterItem : filterItems) {
                FilterBuilder filterItemFilter = getFilterFromFilterItem(filterItem);
                if (filterItemFilter != null)
                    andFilter.add(filterItemFilter);
            }
            return andFilter;
        }
        return null;
    }

    private FilterBuilder getFilterFromFilterItem(FilterItem filterItem) {
        OrFilterBuilder itemFilterBuilder = null;
        switch (filterItem.getType()) {
            case METHOD:
                itemFilterBuilder = FilterBuilders.orFilter();
                for (FilterItemValue value : filterItem.getValues()) {
                    itemFilterBuilder.add(FilterBuilders.hasParentFilter("study", FilterBuilders.termFilter("protocol.analysis_method", value.getText())));
                }
                break;
            case GENOTYPE:
                itemFilterBuilder = FilterBuilders.orFilter();
                for (FilterItemValue value : filterItem.getValues()) {
                    itemFilterBuilder.add(FilterBuilders.hasParentFilter("study", FilterBuilders.termFilter("allele_assay.name", value.getText())));
                }
                break;
            case STUDY:
                itemFilterBuilder = FilterBuilders.orFilter();
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {

                        itemFilterBuilder.add(FilterBuilders.hasParentFilter("study", FilterBuilders.hasParentFilter("phenotype", FilterBuilders.termFilter("_parent", value.getValue()))));
                        //itemFilterBuilder.add(FilterBuilders.hasParentFilter("study",FilterBuilders.termFilter("experiment.id",value.getValue())));
                    } else {
                        itemFilterBuilder.add(FilterBuilders.hasParentFilter("study", FilterBuilders.hasParentFilter("phenotype",
                                FilterBuilders.hasParentFilter("experiment", QueryBuilders.matchQuery("name", value.getText())))));
                        //itemFilterBuilder.add(FilterBuilders.hasParentFilter("study",FilterBuilders.termFilter("experiment.name",value.getText())));
                    }
                }
                break;
            case PHENOTYPE:
                itemFilterBuilder = FilterBuilders.orFilter();
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {
                        itemFilterBuilder.add(FilterBuilders.hasParentFilter("study", FilterBuilders.termFilter("_parent", value.getValue())));
                        //TODO does not work because of some issue   https://groups.google.com/forum/#!topic/elasticsearch/8yTX17uoFiU
                        //itemFilterBuilder.add(FilterBuilders.hasParentFilter("study",FilterBuilders.termFilter("phenotype.id",value.getValue())));
                    } else {

                        itemFilterBuilder.add(FilterBuilders.hasParentFilter("study", FilterBuilders.hasParentFilter("phenotype", QueryBuilders.matchQuery("local_trait_name", value.getText()))));
                        //itemFilterBuilder.add(FilterBuilders.hasParentFilter("study",FilterBuilders.termFilter("phenotype.name",value.getText())));
                    }
                }
                break;
            case ANALYSIS:
                itemFilterBuilder = FilterBuilders.orFilter();
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {
                        itemFilterBuilder.add(FilterBuilders.termFilter("studyid", value.getValue()));
                    } else {
                        itemFilterBuilder.add(FilterBuilders.hasParentFilter("study", FilterBuilders.termFilter("name", value.getText())));
                    }
                }
                break;
            case CANDIDATE_GENE_LIST:
                itemFilterBuilder = FilterBuilders.orFilter();
                boolean hasCandidateGeneLists = false;
                for (FilterItemValue value : filterItem.getValues()) {
                    if (value.getValue() != null) {
                        List<Gene> genes = getCandidateGeneListRanges(value.getValue(), 20000);
                        if (genes == null)
                            continue;
                        hasCandidateGeneLists = true;
                        BoolFilterBuilder rangeOrFilter = FilterBuilders.boolFilter();
                        for (Gene gene : genes) {
                            rangeOrFilter.should(FilterBuilders.boolFilter().must(
                                    FilterBuilders.termFilter("chr", gene.getChr()),
                                    FilterBuilders.rangeFilter("position").from(gene.getStart()).to(gene.getEnd())
                            ));
                        }
                        itemFilterBuilder.add(rangeOrFilter);
                    }
                }
                if (!hasCandidateGeneLists)
                    itemFilterBuilder = null;
                break;

        }

        return itemFilterBuilder;
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
        /*response.get
        List<Integer[]> genes = Lists.newArrayList();
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
        }           */
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
    public MetaSNPAnalysisPage findTopAnalysis(MetaAnalysisTopResultsCriteria criteria, List<FilterItem> filterItems, int start, int size) {
        List<MetaSNPAnalysis> metaSNPAnalysises = Lists.newArrayList();
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());

        FilterBuilder filter = getFilterFromCriteria(criteria, filterItems);
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
                SNPInfo info = new SNPInfo();
                info.setPosition((Integer) fields.get("position").getValue());
                info.setChr((String) fields.get("chr").getValue());
                String annotationString = null;
                //FIXME Change to new annotation format
                info.setInGene((Boolean) fields.get("inGene").getValue());
                if (fields.containsKey("annotation")) {
                    annotationString = fields.get("annotation").getValue();
                }
                info.setAnnotations(convertOldAnnotationTonewFormat(annotationString));
                if (fields.containsKey("annotations.gene_name")) {
                    info.setGene((String) fields.get("annotations.gene_name").getValue());

                }
                MetaSNPAnalysis.Builder metaAnalysisBuilder = new MetaSNPAnalysis.Builder()
                        .setAnalysisId(studyId)
                        .setSnpAnnotation(info)
                        .setpValue((Double) fields.get("score").getValue())
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setOverFDR((Boolean) fields.get("overFDR").getValue())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId());
                if (fields.containsKey("mac")) {
                    metaAnalysisBuilder.setMac((Integer) fields.get("mac").getValue());
                }
                if (fields.containsKey("maf")) {
                    metaAnalysisBuilder.setMaf((Double) fields.get("maf").getValue());
                }
                metaSNPAnalysises.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new MetaSNPAnalysisPage(metaSNPAnalysises, new PageRequest(start / size, size), response.getHits().getTotalHits());
    }

    @Override
    public CandidateGeneListPage findCandidateGeneLists(ConstEnums.TABLE_FILTER filter, String searchString, int page, int size) {
        SearchResponse response = esSearcher.search(filter, null, false, new String[]{"name^3.5", "name.partial^1.5", "description"}, searchString, CandidateGeneList.ES_TYPE, page, size);
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
            esIndexer.index(candidateGeneList);
        } catch (IOException e) {
            e.printStackTrace();
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
        request.setQuery(QueryBuilders.constantScoreQuery(FilterBuilders.idsFilter().addIds(candidateGeneList.getId().toString())));

        NestedBuilder aggrs = AggregationBuilders.nested("genes").path("genes");
        aggrs.subAggregation(AggregationBuilders.terms("annotation").field("genes.annotation").size(5))
                .subAggregation(AggregationBuilders.terms("chr").field("genes.chr").size(5))
                .subAggregation(AggregationBuilders.terms("strand").field("genes.strand").size(2));
        request.addAggregation(aggrs);

        SearchResponse response = request.execute().actionGet();
        if (response.getHits().getTotalHits() > 0) {
            SearchHit hit = response.getHits().getAt(0);
            genes = extractGeneInfos(hit);
        }
        List<ESFacet> statsFacets = Lists.newArrayList();
        List<ESTermsFacet> terms = Lists.newArrayList();
        Nested searchFacets = response.getAggregations().get("genes");
        List<ESFacet> facets = Lists.newArrayList();
        facets.add(new ESFacet(ConstEnums.GENE_FILTER.ALL.name(), 0, genes.size(), 0, null));
        Terms annotationFacet = searchFacets.getAggregations().get("annotation");
        //annotation
        for (Terms.Bucket termEntry : annotationFacet.getBuckets()) {
            ConstEnums.GENE_FILTER annotFilter = getFilterFromFacet(termEntry.getKey());
            if (annotFilter != null) {
                facets.add(new ESFacet(annotFilter.name(), 0, termEntry.getDocCount(), 0, null));
            }
            terms.add(new ESTermsFacet(termEntry.getKey(), termEntry.getDocCount()));
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
            terms.add(new ESTermsFacet(termEntry.getKey(), termEntry.getDocCount()));
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


    private boolean isCandidateGeneListInStudy(Study study, CandidateGeneList candidateGeneList) {
        for (CandidateGeneListEnrichment enrichment : study.getCandidateGeneListEnrichments()) {
            if (enrichment.getCandidateGeneList() == candidateGeneList) {
                return true;
            }
        }
        return false;
    }


    private CandidateGeneListEnrichmentPage findAvailableCandidateGeneListEnrichments(final SecureEntity entity, String searchString, int page, int size) {
        if (entity instanceof CandidateGeneList) {
            SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
            request.setSize(size).setFrom(page).setTypes("study").setNoFields();
            FilterBuilder aclFilter = getAclFilterForEnrichment(entity, false);
            FilterBuilder entityFilter = getEntityFilterForEnrichment(entity);
            FilterBuilder availableFilter = FilterBuilders.notFilter(
                    FilterBuilders.hasChildFilter("candidate_gene_list_enrichment", entityFilter));
            FilterBuilder searchFilter = FilterBuilders.boolFilter().must(aclFilter, availableFilter);

            QueryBuilder query = QueryBuilders.matchAllQuery();
            if (searchString != null && !searchString.equalsIgnoreCase("")) {
                query = multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "allele_assay.name^1.5", "allele_assay.producer", "owner.name", "experiment.name", "phenotype.name");
            }
            request.setQuery(filteredQuery(query, searchFilter));
            SearchResponse response = request.execute().actionGet();
            Set<Long> idsToFetch = ImmutableSet.copyOf(EsSearcher.getIdsFromResponse(response));
            List<Study> studiesFromDb = studyRepository.findAll(idsToFetch);
            Ordering<Study> orderByEs = Ordering.explicit(ImmutableList.copyOf(idsToFetch)).onResultOf(DomainFunctions.getStudyId());
            List<Study> results = orderByEs.immutableSortedCopy(studiesFromDb);

            List<CandidateGeneListEnrichment> enrichments = Lists.newArrayList(Iterables.filter(Lists.transform(results, new Function<Study, CandidateGeneListEnrichment>() {
                @Nullable
                @Override
                public CandidateGeneListEnrichment apply(@Nullable Study study) {
                    if (study != null && !isCandidateGeneListInStudy(study, (CandidateGeneList) entity)) {
                        CandidateGeneListEnrichment enrichment = new CandidateGeneListEnrichment();
                        enrichment.setStudy(study);
                        return enrichment;
                    }
                    return null;
                }
            }), Predicates.notNull()));
            return new CandidateGeneListEnrichmentPage(enrichments, new PageRequest(page, size), (enrichments.size() == 0 ? 0 : response.getHits().getTotalHits()), null);
        } else if (entity instanceof Study) {
            List<String> ids = findCandidateListsCountForStudy(entity.getId());
            SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
            request.setTypes("candidate_gene_list").setNoFields().setSize(size).setFrom(page);
            QueryBuilder query = QueryBuilders.matchAllQuery();
            if (searchString != null && !searchString.equalsIgnoreCase("")) {
                query = multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "allele_assay.name^1.5", "allele_assay.producer", "owner.name", "experiment.name", "phenotype.name");
            }
            request.setQuery(filteredQuery(query, FilterBuilders.boolFilter().must(
                    esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")),
                    FilterBuilders.notFilter(FilterBuilders.idsFilter().addIds(ids.toArray(new String[]{}))))));
            SearchResponse response = request.execute().actionGet();
            Set<Long> idsToFetch = ImmutableSet.copyOf(EsSearcher.getIdsFromResponse(response));

            List<CandidateGeneList> candidateGeneLists = candidateGeneListRepository.findAll(idsToFetch);
            Ordering<CandidateGeneList> orderByEs = Ordering.explicit(ImmutableList.copyOf(idsToFetch)).onResultOf(DomainFunctions.getCandidateGeneListId());
            List<CandidateGeneList> results = orderByEs.immutableSortedCopy(candidateGeneLists);

            List<CandidateGeneListEnrichment> enrichments = Lists.newArrayList(Iterables.filter(Iterables.transform(results, new Function<CandidateGeneList, CandidateGeneListEnrichment>() {
                @Nullable
                @Override
                public CandidateGeneListEnrichment apply(@Nullable CandidateGeneList candidateGeneList) {
                    if (candidateGeneList != null) {
                        CandidateGeneListEnrichment enrichment = new CandidateGeneListEnrichment();
                        enrichment.setStudy((Study) entity);
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
    public CandidateGeneListEnrichmentPage findCandidateGeneListEnrichments(SecureEntity entity, ConstEnums.ENRICHMENT_FILTER currentFilter, String searchString, int page, int size) {

        if (currentFilter == ConstEnums.ENRICHMENT_FILTER.AVAILABLE) {
            return findAvailableCandidateGeneListEnrichments(entity, searchString, page, size);
        }
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(page).setTypes("candidate_gene_list_enrichment").setNoFields();
        FilterBuilder aclFilter = getAclFilterForEnrichment(entity, true);
        FilterBuilder finishedFilter = FilterBuilders.termFilter("status", "Finished");
        FilterBuilder runningFilter = FilterBuilders.termsFilter("status", "Running", "Waiting", "Failed");
        FilterBuilder entityFilter = getEntityFilterForEnrichment(entity);

        FilterBuilder typeFilter = null;
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
        FilterBuilder searchFilter = FilterBuilders.boolFilter().must(aclFilter, entityFilter, typeFilter);

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
    public List<ESFacet> findEnrichmentStats(SecureEntity entity, String searchString) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());

        request.setNoFields().setSize(0);
        FilterBuilder typeFilter = getEntityFilterForEnrichment(entity);
        FilterBuilder aclFilter;
        FilterBuilder availableFilter = null;
        FilterBuilder runningFilter = FilterBuilders.boolFilter().must(typeFilter, FilterBuilders.termsFilter("status", "Running", "Waiting", "Failed"));
        FilterBuilder finishedFilter = FilterBuilders.boolFilter().must(typeFilter, FilterBuilders.termFilter("status", "Finished"));
        if (entity instanceof CandidateGeneList) {
            request.setTypes("study");
            aclFilter = getAclFilterForEnrichment(entity, false);
            availableFilter = FilterBuilders.notFilter(FilterBuilders.hasChildFilter("candidate_gene_list_enrichment", typeFilter));
            finishedFilter = FilterBuilders.hasChildFilter("candidate_gene_list_enrichment", finishedFilter);
            runningFilter = FilterBuilders.hasChildFilter("candidate_gene_list_enrichment", runningFilter);
        } else {
            request.setTypes("candidate_gene_list_enrichment");
            aclFilter = getAclFilterForEnrichment(entity, true);
        }
        request.addAggregation(AggregationBuilders.filter(ConstEnums.ENRICHMENT_FILTER.FINISHED.name()).filter(finishedFilter));
        request.addAggregation(AggregationBuilders.filter(ConstEnums.ENRICHMENT_FILTER.RUNNING.name()).filter(runningFilter));

        if (availableFilter != null) {
            request.addAggregation(AggregationBuilders.filter(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name()).filter(availableFilter));
        }

        QueryBuilder query = QueryBuilders.matchAllQuery();
        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            query = multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "allele_assay.name^1.5", "allele_assay.producer", "owner.name", "experiment.name", "phenotype.name");
        }
        request.setQuery(QueryBuilders.filteredQuery(query, aclFilter));
        SearchResponse response = request.execute().actionGet();


        Aggregations aggregations = response.getAggregations();

        List<ESFacet> facets = Lists.newArrayList();

        Filter filterFacet = aggregations.get(ConstEnums.ENRICHMENT_FILTER.FINISHED.name());
        facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.FINISHED.name(), 0, filterFacet.getDocCount(), 0, null));
        filterFacet = aggregations.get(ConstEnums.ENRICHMENT_FILTER.RUNNING.name());
        facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.RUNNING.name(), 0, filterFacet.getDocCount(), 0, null));

        if (entity instanceof CandidateGeneList) {
            filterFacet = aggregations.get(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name());
            facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name(), 0, filterFacet.getDocCount(), 0, null));
        } else if (entity instanceof Study) {
            long count = findAvailableCandidateGeneListEnrichmentsForStudyCount(entity.getId());
            facets.add(new ESFacet(ConstEnums.ENRICHMENT_FILTER.AVAILABLE.name(), 0, count, 0, null));
        }
        return facets;
    }


    private long findAvailableCandidateGeneListEnrichmentsForStudyCount(Long studyId) {
        List<String> ids = findCandidateListsCountForStudy(studyId);
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setTypes("candidate_gene_list").setNoFields().setSize(0);
        request.setQuery(filteredQuery(matchAllQuery(), FilterBuilders.boolFilter().must(
                esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")),
                FilterBuilders.notFilter(FilterBuilders.idsFilter().addIds(ids.toArray(new String[]{}))))));
        SearchResponse response = request.execute().actionGet();
        return response.getHits().getTotalHits();
    }

    private List<String> findCandidateListsCountForStudy(Long studyId) {
        int count = (int) candidateGeneListEnrichmentsRepository.count();
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());

        request.setTypes("candidate_gene_list_enrichment").addField("candidategenelist.id").setSize(count)
                .setQuery(QueryBuilders.filteredQuery(matchAllQuery(), FilterBuilders.boolFilter().must(
                        esAclManager.getAclFilterForPermissions(Lists.newArrayList("read"), "candidate_gene_list_acl"),
                        FilterBuilders.termFilter("study_.id", studyId)
                )));
        SearchResponse response = request.execute().actionGet();
        List<String> ids = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            Integer id = (Integer) hit.getFields().get("candidategenelist.id").getValue();
            ids.add(String.valueOf(id));
        }
        return ids;
    }

    private FilterBuilder getAclFilterForEnrichment(SecureEntity entity, boolean isInEnrichment) {
        List<String> permissions = Lists.newArrayList("read");
        FilterBuilder studyAclFilter = esAclManager.getAclFilterForPermissions(permissions);
        FilterBuilder aclFilter = (isInEnrichment ? FilterBuilders.hasParentFilter("study", studyAclFilter) : studyAclFilter);

        if (!(entity instanceof CandidateGeneList)) {
            FilterBuilder candidateAcl = esAclManager.getAclFilterForPermissions(permissions, "candidate_gene_list_acl");
            aclFilter = FilterBuilders.boolFilter().must(aclFilter, (isInEnrichment ? candidateAcl : FilterBuilders.hasChildFilter("candidate_gene_list_enrichment", candidateAcl)));
        }
        return aclFilter;
    }

    private FilterBuilder getEntityFilterForEnrichment(SecureEntity entity) {
        FilterBuilder typeFilter = null;
        //TODO change to visitor or strategy pattern
        if (entity instanceof CandidateGeneList) {
            typeFilter = FilterBuilders.termFilter("candidategenelist.id", entity.getId().toString());
        } else if (entity instanceof Experiment) {
            typeFilter = FilterBuilders.termFilter("experiment_.id", entity.getId().toString());
        } else if (entity instanceof TraitUom) {
            typeFilter = FilterBuilders.termFilter("phenotype_.id", entity.getId().toString());
        } else if (entity instanceof Study) {
            typeFilter = FilterBuilders.termFilter("study_.id", entity.getId().toString());
        } else {
            throw new RuntimeException("entity unknown");
        }
        return typeFilter;
    }

    @Override
    @Transactional(readOnly = false)
    public void createCandidateGeneListEnrichments(SecureEntity entity, boolean isAllChecked, List<CandidateGeneListEnrichment> candidateGeneListEnrichments) {
        if (isAllChecked) {
            candidateGeneListEnrichments = getCandidateGeneEnrichmentFromEntity(entity);
        }
        Iterable<CandidateGeneListEnrichment> filteredRecords = Iterables.filter(candidateGeneListEnrichments, new Predicate<CandidateGeneListEnrichment>() {
            @Override
            public boolean apply(@Nullable CandidateGeneListEnrichment candidateGeneListEnrichment) {
                return candidateGeneListEnrichment.getId() == null;
            }
        });
        for (CandidateGeneListEnrichment enrichment : filteredRecords) {
            if (entity instanceof CandidateGeneList) {
                enrichment.setCandidateGeneList((CandidateGeneList) entity);
            } else if (entity instanceof Study) {
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

    private List<CandidateGeneListEnrichment> getCandidateGeneEnrichmentFromEntity(SecureEntity entity) {
        long count = 0;
        if (entity instanceof CandidateGeneList) {
            count = studyRepository.count() * 10;
        } else {
            count = candidateGeneListRepository.count() * 10;
        }
        CandidateGeneListEnrichmentPage page = findCandidateGeneListEnrichments(entity, ConstEnums.ENRICHMENT_FILTER.AVAILABLE, "", 0, (int) count);
        return page.getContents();
    }

    //TODO switch to use ESIndexer Class
    private void indexCandidateGeneListEnrichments(List<CandidateGeneListEnrichment> candidateGeneListEnrichments) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        //necesarry to improve performance
        ImmutableSet<CandidateGeneList> candidateGeneLists = ImmutableSet.copyOf(Iterables.transform(candidateGeneListEnrichments, new Function<CandidateGeneListEnrichment, CandidateGeneList>() {
            @Nullable
            @Override
            public CandidateGeneList apply(@Nullable CandidateGeneListEnrichment candidateGeneListEnrichment) {
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
            e.printStackTrace();
        }
        return null;
    }


    private void deleteCandidateGeneListFromIndex(CandidateGeneList candidateGeneList) {
        esIndexer.delete(candidateGeneList);
        deleteCandidateGeneListEnrichmentsFromIndex(candidateGeneList.getId());
    }

    private void deleteCandidateGeneListEnrichmentsFromIndex(Long candidateGeneListId) {
        try {
            QueryBuilder query = QueryBuilders.constantScoreQuery(FilterBuilders.termFilter("candidategenelist.id", candidateGeneListId));
            client.prepareDeleteByQuery(esAclManager.getIndex()).setTypes("candidate_gene_list_enrichment").setQuery(query).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Gene getGeneById(String id) {
        Gene gene = null;
        client.prepareSearch("annot_chr1", "annot_chr2", "annot_chr3", "annot_chr4", "annot_chr5");
        return gene;
    }

    private List<Gene> extractGeneInfos(SearchHit hit) {
        List<Gene> genes = Lists.newArrayList();
        final Map<String, Object> source = hit.getSource();
        if (source != null && !source.isEmpty()) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) source.get("genes");
            for (Map<String, Object> field : items) {
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
        request.setTypes("candidate_gene_list").setFetchSource("genes", null);
        request.setQuery(QueryBuilders.constantScoreQuery(FilterBuilders.idsFilter().addIds(candidateGeneList.getId().toString())));
        SearchResponse response = request.execute().actionGet();
        if (response.getHits().getTotalHits() > 0) {
            genes = extractGeneInfos(response.getHits().getAt(0));
        }
        return genes;
    }
}

