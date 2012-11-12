package com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.stock;

import com.gwtplatform.mvp.client.View;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.StockManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.stock.StockDetailView.StockDisplayDriver;
import com.gmi.nordborglab.browser.client.util.CustomDataTable;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.Presenter;

import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class StockDetailPresenter extends
		Presenter<StockDetailPresenter.MyView, StockDetailPresenter.MyProxy> {

	public interface MyView extends View {

		void setPedigreeData(CustomDataTable pedigreeData);

		void scheduleLayout();

		StockDisplayDriver getDisplayDriver();
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.stock)
	public interface MyProxy extends ProxyPlace<StockDetailPresenter> {
	}
	
	private StockProxy stock;
	private final PlaceManager placeManager;
	private final CurrentUser currentUser;
	private final StockManager stockManager;

	@Inject
	public StockDetailPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final PlaceManager placeManager, 
			final CurrentUser currentUser, 
			 final StockManager stockManager) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.currentUser = currentUser;
		this.stockManager = stockManager;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, GermplasmPresenter.TYPE_SetMainContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

	@Override
	protected void onReset() {
		super.onReset();
		fireEvent(new LoadingIndicatorEvent(false));
		getView().setPedigreeData(DataTableUtils.createDataTableFromString(stock.getPedigreeData()));
		getView().getDisplayDriver().display(stock);
	}
	
	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		Receiver<StockProxy> receiver = new Receiver<StockProxy>() {
			@Override
			public void onSuccess(StockProxy response) {
				stock = response;
				//fireLoadEvent = true;
				getProxy().manualReveal(StockDetailPresenter.this);
			}

			@Override
			public void onFailure(ServerFailure error) {
				fireEvent(new LoadingIndicatorEvent(false));
				getProxy().manualRevealFailed();
				placeManager.revealPlace(new PlaceRequest(NameTokens.taxonomies));
			}
		};
		try {
			Long stockIdToLoad = Long.valueOf(placeRequest.getParameter("id",
					null));
			if (stock == null || !stock.getId().equals(stockIdToLoad)) {
				stockManager.findOne(receiver, stockIdToLoad);
			} else {
				getProxy().manualReveal(StockDetailPresenter.this);
			}
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new PlaceRequest(NameTokens.taxonomies));
		}
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}
	
	private int getPermission() {
		int permission = 0;
		if (currentUser.isAdmin()) {
			permission = AccessControlEntryProxy.WRITE;
		}
		return permission;
	}
}
