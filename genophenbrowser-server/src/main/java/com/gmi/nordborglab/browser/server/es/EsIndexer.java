package com.gmi.nordborglab.browser.server.es;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnotation;
import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.service.impl.GWASDataTableGenerator;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * Created by uemit.seren on 1/28/15.
 */

@Component
public class EsIndexer {

    @Resource
    protected Client esClient;

    @Resource
    protected AclManager aclManager;

    @Resource
    protected EsAclManager esAclManager;


    public <T extends ESDocument> XContentBuilder getContent(T document) throws IOException {
        XContentBuilder builder = document.getXContent(null);
        if (document instanceof SecureEntity) {
            esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl((SecureEntity) document));
        }
        builder.endObject();
        return builder;
    }

    public <T extends ESDocument> void index(T document) throws IOException {
        index(document, false);
    }

    public <T extends ESDocument> void index(T document, boolean synchronous) throws IOException {
        IndexRequestBuilder request = getIndexRequest(document);

        if (synchronous) {
            request.execute().actionGet();
            refresh();
        } else {
            request.execute();
        }
    }

    public void refresh() {
        refresh(null);
    }

    public void refresh(String... indices) {
        if (indices == null) {
            indices = new String[]{esAclManager.getIndex()};
        }
        esClient.admin().indices().prepareRefresh(indices).execute().actionGet();
    }

    public <T extends ESDocument> void delete(T document) {
        DeleteRequestBuilder request = esClient.prepareDelete(esAclManager.getIndex(), document.getEsType(), document.getEsId());
        if (document.getRouting() != null) {
            request.setRouting(document.getRouting());
        }
        request.execute();
    }

    private <T extends ESDocument> IndexRequestBuilder getIndexRequest(T document) throws IOException {
        XContentBuilder builder = getContent(document);
        IndexRequestBuilder request = esClient.prepareIndex(esAclManager.getIndex(), document.getEsType(), document.getEsId())
                .setSource(builder);
        if (document.getRouting() != null) {
            request.setRouting(document.getRouting());
        }
        if (document.getParentId() != null) {
            request.setParent(document.getParentId());
        }
        return request;
    }

    public <T extends ESDocument> BulkResponse bulkIndex(List<T> documents) throws IOException {
        // Craete bulk indexing
        BulkRequestBuilder bulkRequest = esClient.prepareBulk();
        for (T document : documents) {
            bulkRequest.add(getIndexRequest(document));
        }
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        return bulkResponse;
    }

    public <T extends ESDocument> void index(List<T> documents) throws IOException {
        for (T document : documents) {
            index(document);
        }
    }

    public void updateSecureEntityPermission(SecureEntity entity) throws IOException {
        if (entity.getEsType() == null)
            return;
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl(entity));
        builder.endObject();
        UpdateRequestBuilder request = esClient.prepareUpdate(esAclManager.getIndex(), entity.getEsType(), entity.getId().toString())
                .setDoc(builder);
        if (entity.getRouting() != null) {
            request.setRouting(entity.getRouting());
        }
        request.execute();
    }

    public BulkResponse indexMetaAnalysisSnps(GWASData gwasData, Long studyId,String routing,boolean isBlocking) throws IOException {
        BulkRequestBuilder bulkRequest = esClient.prepareBulk();
        BulkResponse bulkResponse = null;
        for (ChrGWAData chrGWAData : gwasData.getChrGWASData().values()) {
            for (int i = 0; i < chrGWAData.getPositions().length; i++) {
                Character chr = chrGWAData.getChr().charAt(3);
                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject()
                        .field("studyid", studyId)
                        .field("chr", chr)
                        .field("score", chrGWAData.getPvalues()[i])
                        .field("overFDR", chrGWAData.getPvalues()[i] > gwasData.getBonferroniScore())
                        .field("position", chrGWAData.getPositions()[i]);
                if (chrGWAData.getMacs() != null) {
                    builder.field("mac", chrGWAData.getMacs()[i]);
                }
                if (chrGWAData.getMafs() != null) {
                    builder.field("maf", chrGWAData.getMafs()[i]);
                }
                SNPInfo snpInfo = chrGWAData.getSNPInfos().get(i);
                Boolean inGene = snpInfo.isInGene();

                if (snpInfo.getAnnotations() != null && snpInfo.getAnnotations().size() > 0) {
                    builder.field("annotation", GWASDataTableGenerator.getAnnotationFromEffect(snpInfo.getAnnotations().get(0).getEffect()));
                    builder.startArray("annotations");
                    for (SNPAnnotation annotation : snpInfo.getAnnotations()) {
                        builder.startObject()
                                .field("gene_name", annotation.getGene())
                                .field("transcript_id", annotation.getTrascript())
                                .field("effect", annotation.getEffect())
                                .field("function", annotation.getFunction())
                                .field("impact", annotation.getImpact())
                                .field("codon_change", annotation.getCodonChange())
                                .field("rank", annotation.getRank())
                                .field("amino_acid_change", annotation.getAminoAcidChange())
                                .endObject();
                    }
                    builder.endArray();
                }

                builder.field("inGene", inGene)
                        .endObject();
                String id = String.format("%s_%s_%s", studyId, chr, chrGWAData.getPositions()[i]);
                IndexRequestBuilder request = esClient.prepareIndex(esAclManager.getIndex(), "meta_analysis_snps", id)
                        .setSource(builder)
                        .setRouting(routing)
                        .setParent(studyId.toString());
                bulkRequest.add(request);
            }
        }
        if (isBlocking) {
            bulkResponse = bulkRequest.execute().actionGet();
        }
        else {
            bulkRequest.execute();
        }
        return bulkResponse;
    }
}
