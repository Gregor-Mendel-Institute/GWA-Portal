package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.service.OntologyService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.model.Term2Term;
import com.gmi.nordborglab.jpaontology.model.TermPage;
import com.gmi.nordborglab.jpaontology.repository.Term2TermRepository;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.thirdparty.guava.common.collect.Iterables;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */


@Service
@Transactional(readOnly = true)
public class OntologyServiceImpl implements OntologyService {
    private final static String TO_ROOT = "TO:0000387";
    private final static String EO_ROOT = "EO:0007359";

    @Resource
    protected TermRepository termRepository;

    @Resource
    protected Term2TermRepository term2TermRepository;

    @Resource
    protected Client client;

    @Override
    public Term findOne(Integer id) {
        Term term = termRepository.findOne(id);
        return term;
    }

    @Override
    public Term2Term findOneTerm2Term(Integer id) {
        Term2Term term2term = term2TermRepository.findOne(id);
        return term2term;
    }

    @Override
    public Term findRootTerm(String type) {
        Term term = null;
        if ("trait".equalsIgnoreCase(type)) {
            term = termRepository.findByAcc(TO_ROOT);
        } else if ("environment".equalsIgnoreCase(type)) {
            term = termRepository.findByAcc(EO_ROOT);
        }
        return term;
    }

    @Override
    public Term findOneByAcc(String acc) {
        Term term = termRepository.findByAcc(acc);
        List<Integer> pathToRoot = Lists.newArrayList();
        pathToRoot = getPathToRoot(pathToRoot, term);
        pathToRoot = Lists.reverse(pathToRoot);
        term.setPathToRoot(pathToRoot);
        return term;
    }

    @Override
    public TermPage findByQuery(String query, ConstEnums.ONTOLOGY_TYPE type, int limit) {
        query = query.trim();
        List<Term> terms = Lists.newArrayList();
        long numberOfHits = 0;
        FilterBuilder filter = FilterBuilders.termFilter("type", type.name().toLowerCase());
        SearchRequestBuilder request = client.prepareSearch(SearchServiceImpl.ONTOLOGY_INDEX_NAME)
                .setTypes("term").setNoFields()
                .setSize(limit);
        if (query != null && !query.isEmpty()) {
            request.setQuery(QueryBuilders.filteredQuery(QueryBuilders.multiMatchQuery(query, "name^3.5", "name.partial^1.5", "definition", "synonyms", "term_id"), filter));
        } else {
            request.setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filter));
        }
        SearchResponse response = request.execute().actionGet();
        numberOfHits = response.getHits().getTotalHits();
        for (SearchHit hit : response.getHits().getHits()) {
            terms.add(termRepository.findByAcc(hit.getId()));
        }
        return new TermPage(terms, numberOfHits);
    }

    @Override
    public Set<Term> findAllByAcc(Set<String> accs) {
        // FIXME because of https://jira.spring.io/browse/DATAJPA-606
        if (accs == null || accs.isEmpty())
            return Sets.newHashSet();
        return termRepository.findAllByAccIn(accs);
    }

    private List<Integer> getPathToRoot(List<Integer> path, Term term) {
        Preconditions.checkNotNull(path);
        if (term.getParents() != null && term.getParents().size() > 0) {
            Term2Term term2Term = Iterables.get(term.getParents(), 0);
            path.add(term2Term.getId());
            getPathToRoot(path, term2Term.getParent());
        }
        return path;
    }
}
