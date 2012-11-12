package com.gmi.nordborglab.browser.client.mvp.view.main;

import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter.MENU;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.NotificationPopup;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class MainPageView extends ViewImpl implements MainPagePresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, MainPageView> {
	}
	
	interface MyStyle extends CssResource {
	    String current_page_item();
	}
	
	@UiField SimpleLayoutPanel container;
	@UiField Anchor userLink;
	@UiField HTMLPanel userInfoContainer;
	@UiField Label userName;
	@UiField Label userEmail;
	@UiField MyStyle style;
	@UiField InlineHyperlink homeLink;
	@UiField InlineHyperlink diversityLink;
	@UiField InlineHyperlink germplasmLink;
	@UiField InlineHyperlink genotypeLink;
	@UiField DivElement loadingIndicator;
	//@UiField FlowPanel userInfoContainer;
	protected final NotificationPopup notificationPopup = new NotificationPopup();
	private final PlaceManager placeManager;
	private final MainResources resources;
	private AppUserProxy user;

	@Inject
	public MainPageView(final Binder binder,final MainResources resources, final PlaceManager placeManager) {
		widget = binder.createAndBindUi(this);
		this.resources = resources;
		loadingIndicator.getStyle().setDisplay(Display.NONE);
		this.placeManager = placeManager; 
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == MainPagePresenter.TYPE_SetMainContent) {
			setMainContent(content);
		}
		else if (slot == MainPagePresenter.TYPE_SetUserInfoContent) {
			setUserInfoContent(content);
		}
		else {
			super.setInSlot(slot,content);
		}
	}

	private void setMainContent(Widget content) {
		container.setWidget(content);
	}
	
	private void setUserInfoContent(Widget content) {
		//userInfoContainer.add(content);
	}

	@Override
	public String getUserData() {
		String user = null;
		try {
			Dictionary data = Dictionary.getDictionary("data");
			if (data != null) {
				user =  data.get("user");
			}
		}
		catch (Exception e) {}
		return user;
	}

	@Override
	public void showUserInfo(AppUserProxy user) {
		this.user = user;
		if (user == null) {
			userLink.setHref(null);
			userLink.setText("Log In");
			userInfoContainer.setVisible(false);
		}
		else {
			userLink.setHref(null);
			userLink.setHTML("My Account<span class=\""+resources.style().arrow_down()+"\" />");
			userInfoContainer.setVisible(true);
			userEmail.setText(user.getEmail());
			userName.setText(user.getFirstname() + " " + user.getLastname());
		}
	}
	
	@Override
	public void setActiveNavigationItem(MENU menu) {
		String currentPageItemStyleName = style.current_page_item();
		homeLink.removeStyleName(currentPageItemStyleName);
		diversityLink.removeStyleName(style.current_page_item());
		germplasmLink.removeStyleName(style.current_page_item());
		genotypeLink.removeStyleName(style.current_page_item());
		InlineHyperlink currentLink = null;
		switch (menu) {
			case DIVERSITY:
				currentLink = diversityLink;
				break;
			case GENOTYPE:
				currentLink  = genotypeLink;
				break;
			case GERMPLASM:
				currentLink = germplasmLink;
				break;
			case HOME:
				currentLink = homeLink;
			default:
				currentLink = homeLink;
		}
		if (currentLink != null)
			currentLink.addStyleName(currentPageItemStyleName);
	}

	@Override
	public void showNotification(String caption, String message, int level,
			int duration) {
		notificationPopup.setNotificatonContent(caption,message,level);
		notificationPopup.show();
		notificationPopup.center();
	}

	@Override
	public void showLoadingIndicator(boolean show,String text) {
		//text = text + "test";
		loadingIndicator.setInnerText(text);
		loadingIndicator.getStyle().setDisplay(show ? Display.BLOCK : Display.NONE);
	}
	
	@UiHandler("userLink")
	public void onLogin(ClickEvent e) {
		if (user == null) {
			PlaceRequest request = placeManager.getCurrentPlaceRequest();
			Window.Location.assign("login?url=" +placeManager.buildHistoryToken(request));
		}
	}
	
}
