package com.gmi.nordborglab.browser.client.mvp.view.genotype.genome;

import com.gmi.nordborglab.browser.client.mvp.handlers.GenomeBrowserUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.genotype.genome.GenomeBrowserPresenter;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 09.08.13
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class GenomeBrowserView extends ViewWithUiHandlers<GenomeBrowserUiHandlers> implements GenomeBrowserPresenter.MyView {

    interface Binder extends UiBinder<Widget, GenomeBrowserView> {

    }

    private final Widget widget;
    @UiField
    Frame genomeBrowserFrame;

    @Inject
    public GenomeBrowserView(final Binder binder) {
        widget = binder.createAndBindUi(this);
        genomeBrowserFrame.setUrl(Dictionary.getDictionary("appData").get("jBrowseUrl"));
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


}