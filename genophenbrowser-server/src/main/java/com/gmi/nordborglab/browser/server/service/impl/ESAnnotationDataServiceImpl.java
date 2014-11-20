package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.GenotypeReader;
import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GoTerm;
import com.gmi.nordborglab.browser.server.data.annotation.Isoform;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAlleleInfo;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.visualization.datasource.datatable.DataTable;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.springframework.context.annotation.Primary;
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


    private static Pattern geneIdPattern = Pattern.compile("AT([1-5]{1})G\\d+");

    @Resource
    protected GenotypeReader genotypeReader;

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
    public List<SNPAnnot> getSNPAnnotations(String chr, int[] positions) {
        List<SNPAnnot> annotations = new ArrayList<SNPAnnot>();

        MultiGetRequestBuilder requestBuilder = client.prepareMultiGet();
        for (int i = 0; i < positions.length; i++) {
            requestBuilder.add(new MultiGetRequest.Item(String.format(INDEX_PREFIX, chr), "snps", String.valueOf(positions[i])).fields("annotation", "inGene", "ref", "alt").fetchSourceContext(new FetchSourceContext("gene")));
        }
        MultiGetResponse response = requestBuilder.execute().actionGet();
        Iterator<MultiGetItemResponse> iterator = response.iterator();
        MultiGetItemResponse itemResponse = null;
        int failed = 0;
        while (iterator.hasNext()) {
            SNPAnnot snpAnnot = new SNPAnnot();
            try {
                itemResponse = iterator.next();
                snpAnnot.setAnnotation(itemResponse.getResponse().getFields().get("annotation").getValue().toString());
                snpAnnot.setInGene((Boolean) itemResponse.getResponse().getFields().get("inGene").getValue());
                snpAnnot.setRef((String) itemResponse.getResponse().getFields().get("ref").getValue());
                snpAnnot.setAlt((String) itemResponse.getResponse().getFields().get("alt").getValue());
                List<Map<String, Object>> gene = (List<Map<String, Object>>) itemResponse.getResponse().getSourceAsMap().get("gene");
                Map<String, Object> geneFields = gene.get(0);
                snpAnnot.setGene((String) geneFields.get("name"));


            } catch (Exception e) {
                String test = "test";
                failed = failed + 1;
            }
            annotations.add(snpAnnot);
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
            if (response != null) {
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
    public SNPAlleleInfo getSNPAlleleInfo(Long alleleAssayId, Integer chromosome, Integer position, List<Long> passportIds) {
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
        List<Byte> alleles = genotypeReader.getAlleles(genotype, chromosome, position, passportIdsString);
        List<SNPAnnot> snpAnnotations = getSNPAnnotations(String.format("chr%s", chromosome), new int[]{position});
        SNPAlleleInfo info = new SNPAlleleInfo(snpAnnotations.get(0), alleles);
        return info;
    }
}
