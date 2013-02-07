package com.gmi.nordborglab.browser.client.mvp.presenter.home;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class HomePresenter extends
		Presenter<HomePresenter.MyView, HomePresenter.MyProxy> {

	public interface MyView extends View {
        void setLinkToWizard(boolean isLoggedIn);
    }

	@ProxyStandard
    @NameToken(NameTokens.home)
	public interface MyProxy extends ProxyPlace<HomePresenter> {
	}
	
	@ContentSlot
	public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    private final CurrentUser currentUser;

	@Inject
	public HomePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final CurrentUser currentUser) {
		super(eventBus, view, proxy);
        this.currentUser = currentUser;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetMainContent, this);
	}

    @Override
    protected void onReset() {
        super.onReset();
        if (currentUser.isLoggedIn()) {
            getView().setLinkToWizard(true);
        }
        else {
            getView().setLinkToWizard(false);
        }
    }

	@Override
	protected void onBind() {
		super.onBind();
	}
}
