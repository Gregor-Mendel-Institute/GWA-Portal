package com.gmi.nordborglab.browser.client.mvp.widgets.enrichment;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 05.12.13
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListEnrichmentView extends ViewImpl implements CandidateGeneListEnrichmentPresenter.MyView {

    interface Binder extends UiBinder<Widget, CandidateGeneListEnrichmentView> {

    }

    private final Widget widget;


    @UiField
    SimpleLayoutPanel container;

    @Inject
    public CandidateGeneListEnrichmentView(Binder binder) {
        widget = binder.createAndBindUi(this);
        bindSlot(CandidateGeneListEnrichmentPresenter.SLOT_ENRICHMENT, container);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }
}