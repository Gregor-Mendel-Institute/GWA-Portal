package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.OntologyRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyManager extends RequestFactoryManager<OntologyRequest> {

    @Inject
    public OntologyManager(CustomRequestFactory rf) {
        super(rf);
    }

    @Override
    public OntologyRequest getContext() {
        return rf.ontologyRequest();
    }

    public void findOneByAcc(Receiver<TermProxy> receiver, String acc) {
        getContext().findOneByAcc(acc).with("parents.child").fire(receiver);
    }

    public void findOne(Receiver<TermProxy> receiver, Integer id) {
        getContext().findOne(id).with("childs", "childs.term", "childs.term.definition").fire(receiver);
    }

    public void findOneTerm2Term(Receiver<Term2TermProxy> receiver, Integer id) {
        getContext().findOneTerm2Term(id).with("child.childs.child.termDefinition", "child.childs.relationshipType").fire(receiver);
    }

    public void findRootTerm(Receiver<TermProxy> receiver, String type) {
        getContext().findRootTerm(type).with("childs.child.termDefinition", "childs.relationshipType").fire(receiver);
    }

    public void findByQuery(Receiver<TermPageProxy> receiver, String query, ConstEnums.ONTOLOGY_TYPE type, int limit) {
        getContext().findByQuery(query, type, limit).with("contents.termDefinition").fire(receiver);
    }
}
