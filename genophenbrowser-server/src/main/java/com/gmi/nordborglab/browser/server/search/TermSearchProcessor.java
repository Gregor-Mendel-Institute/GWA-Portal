package com.gmi.nordborglab.browser.server.search;


import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public abstract class TermSearchProcessor implements SearchProcessor {
	
	protected String term;
    protected FilterBuilder aclFilter;

    public TermSearchProcessor(String term) {
        this(term, null);
    }

    public TermSearchProcessor(String term, FilterBuilder aclFilter) {
        this.term = term;
        this.aclFilter = aclFilter;
    }

	public Long getLongValue() {
		Long value = null;
		try {
			value =  Long.parseLong(term);
		}
		catch (Exception ex) {
			
		}
		return value;
	}

    protected abstract QueryBuilder getQuery();

    protected QueryBuilder getFilteredQueryBuilder() {
        QueryBuilder query = getQuery();
        if (aclFilter != null) {
            return QueryBuilders.filteredQuery(query, aclFilter);
        }
        return query;
    }

	
}
