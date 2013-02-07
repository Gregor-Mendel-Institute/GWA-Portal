package com.gmi.nordborglab.browser.client.mvp.view.home;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.HomePresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class HomeView extends ViewImpl implements HomePresenter.MyView {

	private final Widget widget;

	@UiField
	SimpleLayoutPanel container;
    @UiField
    Anchor wizardLink;

    private final PlaceManager placeManager;

    public interface Binder extends UiBinder<Widget, HomeView> {

    }
	@Inject
	public HomeView(final Binder binder, final PlaceManager placeManager) {
		widget = binder.createAndBindUi(this);
        this.placeManager = placeManager;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == HomePresenter.TYPE_SetMainContent) {
			setMainContent(content);
		} else {
			super.setInSlot(slot, content);
		}
	}

	private void setMainContent(Widget content) {
		if (content != null) {
			container.setWidget(content);
		}
	}

    @Override
    public void setLinkToWizard(boolean isLoggedIn) {
        String studyWizardLink = placeManager.buildHistoryToken(new PlaceRequest(NameTokens.basicstudywizard));
        String link = "/login?url="+studyWizardLink;
        if (isLoggedIn) {
           link = "#"+studyWizardLink;
        }
        wizardLink.setHref(link);

    }
}
