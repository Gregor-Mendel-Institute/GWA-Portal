package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.service.JPAOntologyService;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.model.Term2Term;
import com.gmi.nordborglab.jpaontology.repository.Term2TermRepository;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Iterables;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */


@Service
@Transactional(readOnly = true)
public class JPAOntologyServiceImpl implements JPAOntologyService {
    private final static String TO_ROOT="TO:0000387";
    private final static String EO_ROOT="EO:0007359";

    @Resource
    protected TermRepository termRepository;

    @Resource
    protected Term2TermRepository term2TermRepository;

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
        }
        else if ("environment".equalsIgnoreCase(type)) {
           term = termRepository.findByAcc(EO_ROOT);
        }
       return term;
    }

    @Override
    public Term findOneByAcc(String acc) {
        Term term = termRepository.findByAcc(acc);
        List<Integer> pathToRoot = Lists.newArrayList();
        pathToRoot = getPathToRoot(pathToRoot,term);
        pathToRoot = Lists.reverse(pathToRoot);
        term.setPathToRoot(pathToRoot);
        return term;
    }

    private List<Integer> getPathToRoot(List<Integer> path,Term term) {
        Preconditions.checkNotNull(path);
        if (term.getParents() != null && term.getParents().size() > 0) {
            Term2Term term2Term = Iterables.get(term.getParents(),0);
            path.add(term2Term.getId());
            getPathToRoot(path,term2Term.getParent());
        }
        return path;
    }
}
