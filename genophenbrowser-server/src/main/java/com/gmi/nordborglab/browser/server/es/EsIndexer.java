package com.gmi.nordborglab.browser.server.es;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
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
        XContentBuilder builder = document.getXContent();
        if (document instanceof SecureEntity) {
            esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl((SecureEntity) document));
        }
        builder.endObject();
        return builder;
    }

    public <T extends ESDocument> void index(T document) throws IOException {
        IndexRequestBuilder request = getIndexRequest(document);
        request.execute();
    }

    public <T extends ESDocument> void delete(T document) {
        esClient.prepareDelete(esAclManager.getIndex(), document.getEsType(), document.getEsId()).execute();
    }

    private <T extends ESDocument> IndexRequestBuilder getIndexRequest(T document) throws IOException {
        XContentBuilder builder = getContent(document);
        IndexRequestBuilder request = esClient.prepareIndex(esAclManager.getIndex(), document.getEsType(), document.getEsId())
                .setSource(builder);
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
        if (entity.getIndexType() == null)
            return;
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl(entity));
        builder.endObject();
        UpdateRequestBuilder request = esClient.prepareUpdate(esAclManager.getIndex(), entity.getIndexType(), entity.getId().toString())
                .setDoc(builder);
        if (entity.getRouting() != null) {
            request.setRouting(entity.getRouting());
        }
        request.execute();
    }


}
