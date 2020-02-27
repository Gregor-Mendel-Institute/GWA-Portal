package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.GenotypeReader;
import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GoTerm;
import com.gmi.nordborglab.browser.server.data.annotation.Isoform;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAlleleInfo;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnotation;
import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.data.es.ESTermsFacet;
import com.gmi.nordborglab.browser.server.domain.DomainFunctions;
import com.gmi.nordborglab.browser.server.domain.genotype.Allele;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.pages.SNPInfoPage;
import com.gmi.nordborglab.browser.server.repository.AlleleAssayRepository;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Bytes;
import com.google.visualization.datasource.datatable.DataTable;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/2/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */

@Primary
@Service("ES")
public class ESAnnotationDataServiceImpl implements AnnotationDataService {


    private static Pattern geneIdPattern = Pattern.compile("^[a-z]{2}([\\d]{1})G\\w+$", Pattern.CASE_INSENSITIVE);
    private static Pattern regionPattern = Pattern.compile("^Chr(\\d{1,2})\\:(\\d+)\\-(\\d+)$", Pattern.CASE_INSENSITIVE);

    @Resource
    protected GenotypeReader genotypeReader;

    @Resource
    protected AlleleAssayRepository alleleAssayRepository;

    @Resource
    protected TraitUomRepository traitUomRepository;

    @Resource
    protected PassportRepository passportRepository;

    @Resource
    protected Client client;
    private static String INDEX_PREFIX = "annot_%s";

    @Override
    public List<Gene> getGenes(String chr, Long start, Long end, boolean isFeatures) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Isoform getGeneIsoform(String gene) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SNPInfo> getSNPAnnotations(String chr, int[] positions) {
        List<SNPInfo> snpInfos = new ArrayList<SNPInfo>();

        MultiGetRequestBuilder requestBuilder = client.prepareMultiGet();
        for (int i = 0; i < positions.length; i++) {
            requestBuilder.add(new MultiGetRequest.Item(String.format(INDEX_PREFIX, chr), "snps", String.valueOf(positions[i])).fields("annotation", "inGene", "ref", "alt").fetchSourceContext(new FetchSourceContext("annotations")));
        }
        MultiGetResponse response = requestBuilder.execute().actionGet();
        Iterator<MultiGetItemResponse> iterator = response.iterator();
        MultiGetItemResponse itemResponse = null;
        int failed = 0;
        while (iterator.hasNext()) {
            SNPInfo snpInfo = new SNPInfo();
            try {
                itemResponse = iterator.next();
                snpInfo.setInGene((Boolean) itemResponse.getResponse().getFields().get("inGene").getValue());
                snpInfo.setRef((String) itemResponse.getResponse().getFields().get("ref").getValue());
                snpInfo.setAlt((String) itemResponse.getResponse().getFields().get("alt").getValue());
                List<Map<String, Object>> annotationMap = (List<Map<String, Object>>) itemResponse.getResponse().getSourceAsMap().get("annotations");
                List<SNPAnnotation> annotations = extractSNPAnnotations(annotationMap);
                snpInfo.setAnnotations(annotations);
                snpInfo.setGene(extractGeneFromAnnotations(annotations));

            } catch (Exception e) {
                String test = "test";
                failed = failed + 1;
            }
            snpInfos.add(snpInfo);
        }
        return snpInfos;
    }

    private String extractGeneFromAnnotations(List<SNPAnnotation> annotations) {
        String gene = null;
        if (annotations != null && annotations.size() > 0) {
            gene = annotations.get(0).getGene();
        }
        return gene;
    }

    private List<SNPAnnotation> extractSNPAnnotations(List<Map<String, Object>> annotationMap) {
        List<SNPAnnotation> annotations = Lists.newArrayList();
        if (annotationMap != null) {
            for (Map<String, Object> an : annotationMap) {
                SNPAnnotation annotation = new SNPAnnotation((String) an.get("effect"));
                if (an.containsKey("impact")) {
                    annotation.setImpact((String) an.get("impact"));
                }
                if (an.containsKey("function")) {
                    annotation.setFunction((String) an.get("function"));
                }
                if (an.containsKey("codon_change")) {
                    annotation.setCodonChange((String) an.get("codon_change"));
                }
                if (an.containsKey("amino_acid_change")) {
                    annotation.setAminoAcidChange((String) an.get("amino_acid_change"));
                }
                if (an.containsKey("gene_name")) {
                    annotation.setGene((String) an.get("gene_name"));
                }
                if (an.containsKey("transcript_id")) {
                    annotation.setTrascript((String) an.get("transcript_id"));
                }
                if (an.containsKey("rank")) {
                    annotation.setRank((Integer) an.get("rank"));
                }
                annotations.add(annotation);
            }

        }
        return annotations;
    }


    @Override
    public Gene getGeneById(String id) {
        Matcher matcher = geneIdPattern.matcher(id);
        Gene gene = null;
        if (matcher.matches()) {
            String chr = "chr" + matcher.group(1);
            GetRequestBuilder builder = client.prepareGet(String.format(INDEX_PREFIX, chr), "gene", id).setFields("name", "chr", "start_pos", "end_pos", "annotation", "strand").setFetchSource("isoforms", null);
            GetResponse response = builder.execute().actionGet();
            if (response != null && response.isExists()) {
                //TODO fix long int boolean stuff
                gene = new Gene(Long.valueOf((Integer) response.getField("start_pos").getValue()), Long.valueOf((Integer) response.getField("end_pos").getValue()), ((Boolean) response.getField("strand").getValue() ? 1 : 0), (String) response.getField("name").getValue(), null);
                gene.setAnnotation((String) response.getField("annotation").getValue());
                if (response.getSourceAsMap().containsKey("isoforms")) {
                    List<Map<String, Object>> isoForms = (List<Map<String, Object>>) response.getSourceAsMap().get("isoforms");
                    if (isoForms != null && isoForms.size() > 0) {
                        Map<String, Object> isoFormFields = isoForms.get(0);
                        gene.setShortDescription((String) isoFormFields.get("short_description"));
                        gene.setCuratorSummary((String) isoFormFields.get("curator_summary"));
                        gene.setDescription((String) isoFormFields.get("description"));
                    }
                }
                if (response.getFields().containsKey("GO")) {
                    List<Object> goTerms = response.getField("GO").getValues();
                    if (goTerms != null && goTerms.size() > 0) {
                        Iterator<Object> iterator = goTerms.iterator();
                        while (iterator.hasNext()) {
                            Map<String, Object> goTermFields = (Map<String, Object>) iterator.next();
                            gene.getGoTerms().add(new GoTerm((String) goTermFields.get("relation"), (String) goTermFields.get("exact"), (String) goTermFields.get("narrow")));
                        }
                    }
                }
            }
        }
        return gene;
    }

    @Override
    public DataTable getGenomeStatData(String stats, String chr) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SNPAlleleInfo getSNPAlleleInfo(Long alleleAssayId, Integer chromosome, Integer position, List<Long> passportIds, boolean fetchPassportInfos) {
        String genotype = alleleAssayId.toString();
        LinkedHashSet<String> passportIdsString = null;
        if (passportIds != null) {
            passportIdsString = Sets.newLinkedHashSet(Lists.transform(passportIds, new com.google.common.base.Function<Long, String>() {
                @Nullable
                @Override
                public String apply(@Nullable Long aLong) {
                    return String.valueOf(aLong);
                }
            }));
        }
        byte[] alleles = genotypeReader.getAlleles(genotype, chromosome, position, passportIdsString);
        List<SNPInfo> snpAnnotations = getSNPAnnotations(String.format("chr%s", chromosome), new int[]{position});
        List<Long> ids2Fetch = Lists.transform(genotypeReader.getAccessionIds(genotype, passportIdsString), new Function<String, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable String input) {
                return Long.valueOf(input);
            }
        });
        List<Passport> passports = null;
        if (fetchPassportInfos) {
            passports = passportRepository.findAll(ids2Fetch);
            Ordering<Passport> orderByIds = Ordering.explicit(ids2Fetch).onResultOf(DomainFunctions.getPassportId());
            passports = orderByIds.immutableSortedCopy(passports);
        }
        SNPInfo snpInfo = snpAnnotations.get(0);
        snpInfo.setChr(chromosome.toString());
        snpInfo.setPosition(position);
        SNPAlleleInfo info = new SNPAlleleInfo(snpInfo, Bytes.asList(alleles), passports);
        return info;
    }

    @Override
    public SNPInfoPage getSNPInfosForFilter(Long alleleAssayId, String region, int page, int size, List<Long> passportIds) {
        SNPInfoPage snpInfoPage = null;
        if (alleleAssayId == null)
            throw new RuntimeException("genotype not specified");

        Matcher geneMatcher = geneIdPattern.matcher(region);
        Matcher regionMatcher = regionPattern.matcher(region);
        if (!geneMatcher.matches() && (!regionMatcher.matches() || regionMatcher.groupCount() != 3))
            throw new RuntimeException(String.format("Region %s invalid", region));
        int start = 0;
        int end = 0;
        Integer chr = null;
        if (geneMatcher.matches()) {
            Gene gene = getGeneById(region);
            if (gene == null) {
                throw new RuntimeException("Gene not found in index");
            }
            start = (int) gene.getStart();
            end = (int) gene.getEnd();
            chr = Integer.valueOf(gene.getChr());
        } else {
            chr = Integer.valueOf(regionMatcher.group(1));
            start = Integer.valueOf(regionMatcher.group(2));
            end = Integer.valueOf(regionMatcher.group(3));

        }
        QueryBuilder filter = QueryBuilders.rangeQuery("position").from(start).to(end);
        SearchRequestBuilder requestBuilder = client.prepareSearch(String.format(INDEX_PREFIX, "chr" + chr))
                .addSort("position", SortOrder.ASC)
                //FIXME remove lyr if everything is indexed with anc
                .setSize(size).setFrom(page).addFields("annotation", "inGene", "ref", "alt", "anc", "lyr").setFetchSource("annotations", null)
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery()).filter(filter));

        // Add aggregation
        requestBuilder.addAggregation(AggregationBuilders.terms("inGene").field("inGene").size(2))
                .addAggregation(AggregationBuilders.nested("annotations").path("annotations")
                        .subAggregation(AggregationBuilders.terms("impact").field("impact").size(3))
                        .subAggregation(AggregationBuilders.terms("function").field("function").size(4))
                        .subAggregation(AggregationBuilders.terms("effect").field("effect")));


        SearchResponse response = requestBuilder.execute().actionGet();
        List<SNPInfo> snpInfos = Lists.newArrayList();
        long total = response.getHits().getTotalHits();
        for (SearchHit hit : response.getHits()) {
            SNPInfo snpInfo = new SNPInfo();
            try {
                //FIXME reindex data
                snpInfo.setInGene(hit.getFields().get("inGene").<Integer>getValue() != 0);
                snpInfo.setRef(hit.getFields().get("ref").<String>getValue());
                snpInfo.setAlt(hit.getFields().get("alt").<String>getValue());
                //FIXME remove lyr branch if data is re-indexed
                if (hit.getFields().containsKey("anc")) {
                    snpInfo.setAnc(hit.getFields().get("anc").<String>getValue());
                } else {
                    snpInfo.setAnc(hit.getFields().get("lyr").<String>getValue());
                }
                snpInfo.setPosition(Long.parseLong(hit.getId()));
                snpInfo.setChr(chr.toString());
                List<Map<String, Object>> annotationMap = (List<Map<String, Object>>) hit.getSource().get("annotations");
                List<SNPAnnotation> annotations = extractSNPAnnotations(annotationMap);
                snpInfo.setAnnotations(annotations);
                snpInfo.setGene(extractGeneFromAnnotations(annotations));

            } catch (Exception e) {
                String test = "test";
            }
            snpInfos.add(snpInfo);
        }
        List<ESFacet> facets = Lists.newArrayList();
        //Extract aggregations
        Aggregations aggregations = response.getAggregations();

        // get inGene facet
        Terms searchFacet = aggregations.get("inGene");
        List<ESTermsFacet> terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : searchFacet.getBuckets()) {
            String term = "intergenic";
            if (termEntry.getKeyAsString().equalsIgnoreCase("T")) {
                term = "genic";
            }
            terms.add(new ESTermsFacet(term, termEntry.getDocCount()));
        }
        facets.add(new ESFacet("inGene", 0, 0, 0, terms));
        //
        Aggregations subAggregations = ((Nested) aggregations.get("annotations")).getAggregations();

        facets.add(getTermFacet(subAggregations, "effect"));
        facets.add(getTermFacet(subAggregations, "function"));
        facets.add(getTermFacet(subAggregations, "impact"));

        if (alleleAssayId != null) {
            LinkedHashSet<String> passportIdSet = null;
            final AlleleAssay alleleAssay = alleleAssayRepository.findOne(alleleAssayId);
            int totalAlleles = alleleAssay.getAlleles().size();
            if (passportIds != null) {
                final ImmutableSet<Long> allelePassportLookup = FluentIterable.from(alleleAssay.getAlleles()).transform(new Function<Allele, Long>() {
                    @Override
                    public Long apply(Allele input) {
                        Preconditions.checkNotNull(input);
                        Preconditions.checkNotNull(input.getPassport());
                        return input.getPassport().getId();
                    }
                }).toSet();
                passportIdSet = Sets.newLinkedHashSet(FluentIterable.from(passportIds)
                        .filter(Predicates.in(allelePassportLookup))
                        .transform(new Function<Long, String>() {
                            @Override
                            public String apply(Long input) {
                                Preconditions.checkNotNull(input);
                                return input.toString();
                            }
                        }));
                totalAlleles = passportIdSet.size();
            }
            int[] allelesCount = genotypeReader.getAlleleCount(alleleAssayId.toString(), chr, start, end, passportIdSet);
            int[] positions = genotypeReader.getPositions(alleleAssayId.toString(), chr, start, end);
            Map<Integer, Integer> lookUpMap = Maps.newHashMap();
            for (int i = 0; i < positions.length; i++) {
                lookUpMap.put(positions[i], i);
            }

            for (SNPInfo info : snpInfos) {
                if (lookUpMap.containsKey((int) info.getPosition())) {
                    int altCount = allelesCount[lookUpMap.get((int) info.getPosition())];
                    info.setAltCount(altCount);
                    info.setRefCount(totalAlleles - altCount);
                }
            }
        }

        snpInfoPage = new SNPInfoPage(snpInfos, new PageRequest(page, size), total, facets);
        return snpInfoPage;
    }


    private ESFacet getTermFacet(Aggregations aggregation, String name) {
        Terms facet = aggregation.get(name);
        List<ESTermsFacet> terms = Lists.newArrayList();
        for (Terms.Bucket termEntry : facet.getBuckets()) {
            terms.add(new ESTermsFacet(termEntry.getKeyAsString(), termEntry.getDocCount()));
        }
        return new ESFacet("function", 0, 0, 0, terms);
    }
}
