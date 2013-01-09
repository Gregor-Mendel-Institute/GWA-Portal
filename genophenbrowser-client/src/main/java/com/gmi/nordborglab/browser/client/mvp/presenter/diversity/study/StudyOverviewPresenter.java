package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class StudyOverviewPresenter
		extends
		Presenter<StudyOverviewPresenter.MyView, StudyOverviewPresenter.MyProxy> {

	public interface MyView extends View {

		HasData<StudyProxy> getDisplay();
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.studyoverview)
	public interface MyProxy extends ProxyPlace<StudyOverviewPresenter> {
	}
	
	protected final AsyncDataProvider<StudyProxy> dataProvider;
	protected final CdvManager cdvManager;

	@Inject
	public StudyOverviewPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy,final CdvManager cdvManager) {
		super(eventBus, view, proxy);
		this.cdvManager = cdvManager;
		dataProvider = new AsyncDataProvider<StudyProxy>() {

			@Override
			protected void onRangeChanged(HasData<StudyProxy> display) {
				requestStudies(display.getVisibleRange());
			}
		};
		dataProvider.addDataDisplay(getView().getDisplay());
	}
	
	protected void requestStudies(final Range range) {
		fireEvent(new LoadingIndicatorEvent(true));
		Receiver<StudyPageProxy> receiver = new Receiver<StudyPageProxy>() {
			@Override
			public void onSuccess(StudyPageProxy studies) {
				fireEvent(new LoadingIndicatorEvent(false));
				dataProvider.updateRowCount((int)studies.getTotalElements(), true);
				dataProvider.updateRowData(range.getStart(), studies.getContent());
			}
		};
		cdvManager.findAll(receiver,null,null,null,null,null,range.getStart(),range.getLength());
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, DiversityPresenter.TYPE_SetMainContent, this);
	}

	@Override
	protected void onReset() {
		super.onReset();
	}
}
