package com.gmi.nordborglab.browser.client.mvp.presenter.genotype.genome;

import com.gmi.nordborglab.browser.client.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.mvp.handlers.GenomeBrowserUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 09.08.13
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class GenomeBrowserPresenter extends Presenter<GenomeBrowserPresenter.MyView, GenomeBrowserPresenter.MyProxy> implements GenomeBrowserUiHandlers {


    public interface MyView extends View, HasUiHandlers<GenomeBrowserUiHandlers> {

    }

    @ProxyCodeSplit
    @NameToken(NameTokens.genomebrowser)
    public interface MyProxy extends ProxyPlace<GenomeBrowserPresenter> {
    }


    @Inject
    public GenomeBrowserPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
    }


    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetMainContent, this);
    }
}
