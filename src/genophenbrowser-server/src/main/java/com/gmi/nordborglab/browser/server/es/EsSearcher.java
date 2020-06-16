package com.gmi.nordborglab.browser.server.es;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by uemit.seren on 1/29/15.
 */
@Component
public class EsSearcher {

    @Resource
    protected Client esClient;

    @Resource
    protected EsAclManager esAclManager;

    public static final Function<SearchHit, Long> searchHit2LongFunc = new Function<SearchHit, Long>() {
        @Nullable
        @Override
        public Long apply(SearchHit input) {
            return Long.parseLong(input.getId());
        }
    };

    public SearchResponse search(ConstEnums.TABLE_FILTER filter, boolean noPublic, String[] fields, String searchString, String type, int start, int size) {
        return search(filter, null, null, noPublic, fields, searchString, type, start, size);
    }

    public SearchResponse search(ConstEnums.TABLE_FILTER filter, Long parentId, String parentType, boolean noPublic, String[] fields, String searchString, String type, int start, int size) {
        SearchRequestBuilder request = esClient.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(start).setTypes(type).setNoFields();

        // filter only the ones We have access to.

        QueryBuilder preFilter = esAclManager.getAclFilterForPermissions(Lists.newArrayList("read"), noPublic);

        // filter by parentid
        if (parentId != null && parentType != null) {
            preFilter = QueryBuilders.boolQuery().filter(QueryBuilders.hasParentQuery(parentType, QueryBuilders.termQuery("_id", parentId))).filter(preFilter);
        }
        QueryBuilder searchFilter = null;
        org.elasticsearch.index.query.QueryBuilder query = QueryBuilders.matchAllQuery();
        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            query = QueryBuilders.multiMatchQuery(searchString, fields);
        }
        request.setQuery(QueryBuilders.boolQuery().must(query).filter(preFilter));

        QueryBuilder privateFilter = esAclManager.getAclFilterForType(ConstEnums.TABLE_FILTER.PRIVATE);
        QueryBuilder sharedFilter = esAclManager.getAclFilterForType(ConstEnums.TABLE_FILTER.SHARED);
        QueryBuilder publicFilter = esAclManager.getAclFilterForType(ConstEnums.TABLE_FILTER.PUBLISHED);

        request.addAggregation(AggregationBuilders.filter(ConstEnums.TABLE_FILTER.ALL.name()).filter(preFilter));
        request.addAggregation(AggregationBuilders.filter(ConstEnums.TABLE_FILTER.PRIVATE.name()).filter(privateFilter));
        request.addAggregation(AggregationBuilders.filter(ConstEnums.TABLE_FILTER.SHARED.name()).filter(sharedFilter));
        request.addAggregation(AggregationBuilders.filter(ConstEnums.TABLE_FILTER.PUBLISHED.name()).filter(publicFilter));


        switch (filter) {
            case PRIVATE:
                searchFilter = privateFilter;
                break;
            case PUBLISHED:
                searchFilter = publicFilter;
                break;
            case SHARED:
                searchFilter = sharedFilter;
                break;
            case RECENT:
                request.addSort("modified", SortOrder.DESC);
                break;
            default:
                if (searchString == null || searchString.isEmpty())
                    request.addSort("name", SortOrder.ASC);
        }
        // set filter
        request.setPostFilter(searchFilter);

        SearchResponse response = request.execute().actionGet();

        return response;
    }

    public static List<Long> getIdsFromResponse(SearchResponse response) {
        return FluentIterable.from(response.getHits()).transform(searchHit2LongFunc).toList();
    }

    public static List<ESFacet> getAggregations(SearchResponse response) {
        Aggregations aggregations = response.getAggregations();
        Filter filter = null;
        List<ESFacet> facets = Lists.newArrayList();
        filter = aggregations.get(ConstEnums.TABLE_FILTER.ALL.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.ALL.name(), 0, filter.getDocCount(), 0, null));
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.RECENT.name(), 0, filter.getDocCount(), 0, null));

        filter = aggregations.get(ConstEnums.TABLE_FILTER.PRIVATE.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.PRIVATE.name(), 0, filter.getDocCount(), 0, null));

        filter = aggregations.get(ConstEnums.TABLE_FILTER.PUBLISHED.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.PUBLISHED.name(), 0, filter.getDocCount(), 0, null));

        filter = aggregations.get(ConstEnums.TABLE_FILTER.SHARED.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.SHARED.name(), 0, filter.getDocCount(), 0, null));
        // get annotation
        return facets;
    }

    @PostConstruct
    public void addPlugins() {

    }


}
