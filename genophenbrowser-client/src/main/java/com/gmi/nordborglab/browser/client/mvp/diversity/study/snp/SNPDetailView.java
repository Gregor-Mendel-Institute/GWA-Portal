package com.gmi.nordborglab.browser.client.mvp.diversity.study.snp;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class SNPDetailView
        extends ViewWithUiHandlers<SNPDetailUiHandlers> implements SNPDetailPresenter.MyView {



    interface Binder extends UiBinder<Widget, SNPDetailView> {

    }


    @Inject
    SNPDetailView(final Binder binder) {
        initWidget(binder.createAndBindUi(this));
        bindSlot(SNPDetailPresenter.SLOT_SNP_DETAIL, asWidget());
    }


}
