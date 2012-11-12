package com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport;

import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.PassportManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport.PassportListPresenter.PassportProxyFilter;
import com.gmi.nordborglab.browser.shared.proxy.PassportPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;


public class PassportDataProvider extends AsyncDataProvider<PassportProxy> implements HasHandlers{
	
	private final PassportManager passportManager;
	private final EventBus eventBus;
	private PassportPageProxy data;
	private ImmutableList<PassportProxy> filteredView;
	private Long taxonomyId;
	private int size = 250;
	private boolean isPolling = false;
	int start = 0;
	private PassportProxyFilter passportProxyFilter;
	
	private Receiver<PassportPageProxy> receiver = new Receiver<PassportPageProxy>() {

		@Override
		public void onSuccess(PassportPageProxy response) {
			fireEvent(new LoadingIndicatorEvent(false));
			isPolling = false;
			data = response;
			filteredView = null;
			passportProxyFilter.setExpanding(false);
			updateView();
			
		}
	};
	
	
	@Inject
	public PassportDataProvider(final PassportManager passportManager, final EventBus eventBus) {
		this.passportManager = passportManager;
		this.eventBus = eventBus;
	}
	
	@Override
	protected void onRangeChanged(HasData<PassportProxy> display) {
		requestData(display);
	}
	
	public void setTaxonomyId(Long taxonomyId) {
		if (this.taxonomyId != null && this.taxonomyId == taxonomyId) 
			return;
		this.taxonomyId = taxonomyId;
		data = null;
	}
	
	private void updateView() {
		if (data == null)
			return;
		if (filteredView == null) {
			updateRowCount((int) data.getTotalElements(), true);
			updateRowData(start, data.getContent());
		}
		else {
			updateRowCount((int) filteredView.size(), true);
			updateRowData(start, filteredView);
		}
		passportProxyFilter.setDirty(false);
		isPolling = false;
	}
	
	private void requestData(HasData<PassportProxy> display) {
		if (taxonomyId == null)
			return;
		if (!isPolling) {
			final Range range = display.getVisibleRange();
			boolean isRequestRequired = isRequestRequired(range);
			if ((passportProxyFilter.isDirty() && (isRequestRequired || hasMore())) || isRequestRequired || passportProxyFilter.isExpanding()) {
				start = range.getStart();
				isPolling = true;
				fireEvent(new LoadingIndicatorEvent(true));
				passportManager.findAll(receiver, taxonomyId, passportProxyFilter, start, size);
			}
			else {
				clientSideFiltering();
				updateView();
			}
			passportProxyFilter.setDirty(false);
		}
	}
	
	private boolean isContained(Range range) {
		if (data == null)
			return false;
		if (data.getContent().size() == data.getTotalElements())
			return true;
		if ((range.getStart() + range.getLength()) > (start + data.getContent().size()) ||
		    (range.getStart() < start))
			return false;
		return true;
	}
	
	public boolean hasMore() {
		if (data == null || data.getContent().size() < data.getTotalElements())
			return true;
		return false;
	}
	
	private boolean isRequestRequired(Range range) {
		return data == null || !isContained(range);
	}
	
	private void clientSideFiltering() {
		if (!passportProxyFilter.isDirty())
			return;
		filteredView = ImmutableList.copyOf(Collections2.filter(data.getContent(),Predicates.and(passportProxyFilter.getPredicates())));
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEvent(event);
		
	}

	public void setPassportProxyFilter(PassportProxyFilter passportProxyFilter) {
		this.passportProxyFilter = passportProxyFilter;
	}
}
