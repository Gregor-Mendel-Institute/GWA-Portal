package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 24.09.13
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class LoadCandidateGeneListEvent extends GwtEvent<LoadCandidateGeneListEvent.LoadCandidateGeneListHandler> {

    public interface LoadCandidateGeneListHandler extends EventHandler {
        void onLoadCandidateGeneList(LoadCandidateGeneListEvent event);
    }

    public static final Type<LoadCandidateGeneListHandler> TYPE = new Type<LoadCandidateGeneListHandler>();

    private final CandidateGeneListProxy candidateGeneList;

    public LoadCandidateGeneListEvent(CandidateGeneListProxy candidateGeneList) {
        this.candidateGeneList = candidateGeneList;
    }

    @Override
    public Type<LoadCandidateGeneListHandler> getAssociatedType() {
        return TYPE;
    }

    public static Type<LoadCandidateGeneListHandler> getType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LoadCandidateGeneListHandler handler) {
        handler.onLoadCandidateGeneList(this);
    }

    public static void fire(final HasHandlers source,
                            CandidateGeneListProxy candidateGeneList) {
        source.fireEvent(new LoadCandidateGeneListEvent(candidateGeneList));
    }

    public CandidateGeneListProxy getCandidateGeneList() {
        return candidateGeneList;
    }

}
