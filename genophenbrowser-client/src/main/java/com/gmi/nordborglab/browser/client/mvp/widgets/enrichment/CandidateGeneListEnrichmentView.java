package com.gmi.nordborglab.browser.client.mvp.widgets.enrichment;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
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
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == CandidateGeneListEnrichmentPresenter.TYPE_SetCandidateGeneListEnrichment) {
            container.setWidget(content);
        } else {
            super.setInSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}