package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.domain.ontology.BasicTerm2Term;
import com.gmi.nordborglab.browser.server.domain.ontology.IsATerm;
import com.gmi.nordborglab.browser.server.domain.ontology.Term;
import com.gmi.nordborglab.browser.server.repository.ontology.BasicTerm2TermRepository;
import com.gmi.nordborglab.browser.server.repository.ontology.TermNeoRepository;
import com.gmi.nordborglab.browser.server.service.GraphOntologyService;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.jpaontology.model.Term2Term;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/8/13
 * Time: 5:40 PM
 * To change this template use File | Settings | File Templates.
 */

@Service
@Transactional(readOnly = true)
public class GraphOntologyServiceImpl implements GraphOntologyService {

    private final static String TO_ROOT="TO:0000387";
    private final static String EO_ROOT="EO:0007359";

    @Resource
    protected TermNeoRepository termNeoRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Resource
    protected Neo4jTemplate template;

    @Resource
    private BasicTerm2TermRepository basicTerm2TermRepository;

    @Override
    public Term findRootTerm(String type) {
        Term term = null;
        if ("trait".equalsIgnoreCase(type)) {
            term = termNeoRepository.findById(TO_ROOT);
        }
        else if ("environment".equalsIgnoreCase(type)) {
            term = termNeoRepository.findById(EO_ROOT);
        }
        if (term != null) {
            for (BasicTerm2Term term2Term : term.getChildren()) {
                fetchTerm(term2Term.getChild());
            }
        }

        return term;
    }

    @Override
    public Term findOneByAcc(String acc) {
        Term term = termNeoRepository.findById(acc);
        //fetchTerm(term);
        //TODO because of a bug the path can't be extracted directly

            List<Long> pathToRoot = Lists.newArrayList();
            pathToRoot = getPathToRoot(pathToRoot,term);
            term.setPathToRoot(pathToRoot);

        return term;
    }

    @Override
    public BasicTerm2Term findOneTerm2Term(Long id) {
        BasicTerm2Term term2Term = basicTerm2TermRepository.findOne(id);
        fetchTerm(term2Term.getChild());
        for (BasicTerm2Term subTerm2Term:term2Term.getChild().getChildren()) {
            fetchTerm(subTerm2Term.getChild());
        }
        return term2Term;
    }


    private List<Long> getPathToRoot(List<Long> path, Term term) {
        Preconditions.checkNotNull(path);
        if (term.getParents() != null && term.getParents().size() > 0) {
            BasicTerm2Term term2Term =  Iterables.get(term.getParents(), 0);
            path.add(term2Term.getNodeId());
            fetchTerm(term2Term.getParent());
            getPathToRoot(path,term2Term.getParent());
        }
        return path;
    }


    private void fetchTerm(Term term) {
        template.fetch(term);
    }
}
