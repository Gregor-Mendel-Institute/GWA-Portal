package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.GraphOntologyRequest;
import com.gmi.nordborglab.browser.shared.service.OntologyRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphOntologyManager extends RequestFactoryManager<GraphOntologyRequest> {

    @Inject
    public GraphOntologyManager(CustomRequestFactory rf) {
        super(rf);
    }

    @Override
    public GraphOntologyRequest getContext() {
        return rf.graphOntologyRequest();
    }

    public void findOneByAcc(Receiver<GraphTermProxy> receiver,String acc) {
        getContext().findOneByAcc(acc).with("parents.child").fire(receiver);
    }

    /*public void findOneTerm2Term(Receiver<Term2TermProxy> receiver,Integer id) {
        getContext().findOneTerm2Term(id).with("child.childs.child.termDefinition","child.childs.relationshipType").fire(receiver);
    } */

    public void findRootTerm(Receiver<GraphTermProxy> receiver, String type) {
        getContext().findRootTerm(type).with("children.child").fire(receiver);
    }

    public void findOneTerm2Term(Receiver<GraphTerm2TermProxy> receiver, Long id) {
        getContext().findOneTerm2Term(id).with("child.children.child").fire(receiver);
    }
}
