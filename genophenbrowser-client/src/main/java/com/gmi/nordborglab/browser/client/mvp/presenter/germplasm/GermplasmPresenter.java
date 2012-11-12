package com.gmi.nordborglab.browser.client.mvp.presenter.germplasm;

import java.util.List;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.HelperManager;
import com.gmi.nordborglab.browser.client.manager.TaxonomyManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.shared.proxy.BreadcrumbItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.common.collect.ImmutableList;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class GermplasmPresenter extends
		Presenter<GermplasmPresenter.MyView, GermplasmPresenter.MyProxy> {

	public interface MyView extends View {

		void setTitle(String title);

		void clearBreadcrumbs(int size);
		void setBreadcrumbs(int index, String title, String historyToken);

		void initMenu(ImmutableList<TaxonomyProxy> taxonomies);

		void setActiveMenuItem(Long selectedTaxonomyId,Long alleleAssayId);
	}

	@ProxyCodeSplit
	public interface MyProxy extends Proxy<GermplasmPresenter> {
	}
	
	@ContentSlot
	public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();
	
	private final PlaceManager placeManager;
	private final HelperManager helperManager;
	private final TaxonomyManager taxonomyManager;
	protected ImmutableList<TaxonomyProxy> taxonomies = null;
	protected String titleType = null;
	protected Long titleId = null;
	private Long selectedTaxonomyId = null;
	private Long alleleAssayId = null;
	

	@Inject
	public GermplasmPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy,final PlaceManager placeManager, 
			final HelperManager helperManager, final TaxonomyManager taxonomyManager) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.helperManager = helperManager;
		this.taxonomyManager = taxonomyManager;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetMainContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

	@Override
	protected void onReset() {
		super.onReset();
		PlaceRequest request = placeManager.getCurrentPlaceRequest();
		if (request.matchesNameToken(NameTokens.taxonomies)) {
			selectedTaxonomyId = null;
		}
		else if (request.matchesNameToken(NameTokens.taxonomy)) {
			selectedTaxonomyId = Long.parseLong(request.getParameter("id", ""));
			alleleAssayId = null;
		}
		else if (request.matchesNameToken(NameTokens.passports)) {
			selectedTaxonomyId = Long.parseLong(request.getParameter("id", ""));
			String alleleAssay = request.getParameter("alleleAssayId", null);
			if (alleleAssay != null) 
				alleleAssayId = Long.parseLong(alleleAssay);
		}
		else if (request.matchesNameToken(NameTokens.passport) || request.matchesNameToken(NameTokens.stock)) {
			if (alleleAssayId == null)
				alleleAssayId = 0L;
		}
	
		
		if (taxonomies == null) {
			fireEvent(new LoadingIndicatorEvent(true));
			taxonomyManager.findAll(new Receiver<List<TaxonomyProxy>>() {

				@Override
				public void onSuccess(List<TaxonomyProxy> response) {
					fireEvent(new LoadingIndicatorEvent(false));
					taxonomies = ImmutableList.copyOf(response);
					getView().initMenu(taxonomies);
					getView().setActiveMenuItem(selectedTaxonomyId, alleleAssayId);
				}
			});
		}
		else {
			getView().initMenu(taxonomies);
			getView().setActiveMenuItem(selectedTaxonomyId,alleleAssayId);
		}
		setTitle();
	}
	
	protected void setTitle() {
		PlaceRequest request = placeManager.getCurrentPlaceRequest();
		String type = null;
		String title ="Taxonomies";
		if (request.matchesNameToken(NameTokens.taxonomies)) 
			getView().clearBreadcrumbs(0);
		if (request.matchesNameToken(NameTokens.taxonomy)) {
			type = "taxonomy";
		}
		else if (request.matchesNameToken(NameTokens.passports) ) {
			title = "Passports";
			type = "passports";
		}
		else if(request.matchesNameToken(NameTokens.passport)) {
			title = "Passport";
			type ="passport";
		}
		else if (request.matchesNameToken(NameTokens.stock)) {
			title="Stock";
			type = "stock";
		}
		getView().setTitle(title);
		Long id = null;
		try {
			id = Long.parseLong(request.getParameter("id", null));
		}
		catch (Exception e ) {
			
		}
		if (!titleUpdateRequired(type, id))
			return;
		helperManager.getBreadcrumbs(new Receiver<List<BreadcrumbItemProxy>>() {
			
			@Override
			public void onSuccess(List<BreadcrumbItemProxy> response) {
				getView().clearBreadcrumbs(response.size());
				getView().setBreadcrumbs(0,"ALL", placeManager.buildHistoryToken(new PlaceRequest(NameTokens.taxonomies)));
				for (int i=0;i<response.size();i++) {
					BreadcrumbItemProxy item = response.get(i);
					String nameToken = null;
					if (item.getType().equals("taxonomy"))
						nameToken = NameTokens.taxonomy;
					else if (item.getType().equals("passports"))
						nameToken = NameTokens.passports;
					else if (item.getType().equals("passport"))
						nameToken = NameTokens.passport;
					else if (item.getType().equals("stock"))
						nameToken = NameTokens.stock;
					PlaceRequest request = new ParameterizedPlaceRequest(nameToken).with("id", item.getId().toString());
					getView().setBreadcrumbs(i+1,item.getText(),placeManager.buildHistoryToken(request));
				}
			}
		},id,type);
	}
	
	protected boolean titleUpdateRequired(String type,Long id) {
		boolean required = false;
		if (type != null) {
			if (!type.equals(titleType)) {
				if (id != null)
					required = true;
			}
			else if (id != null && !id.equals(titleId)) {
				required = true;
			}
		}
		titleType = type;
		titleId = id;
		return required;
	}
}
