package com.gmi.nordborglab.browser.client.mvp.view.main;

import com.gwtplatform.mvp.client.ViewImpl;
//import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.UserInfoPresenter;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.gwt.client.impl.JsoSplittable;
import com.google.web.bindery.autobean.shared.Splittable;

public class UserInfoView extends ViewImpl implements UserInfoPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, UserInfoView> {
	}
	
	@UiField HTMLPanel userInfo;
	@UiField Anchor signinLink;
	@UiField Anchor signoutLink;
	@UiField Label username;

	@Inject
	public UserInfoView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	


	@Override
	public String getUserdata() {
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
		boolean signedIn = user != null;
		signoutLink.setVisible(signedIn);
		signinLink.setVisible(!signedIn);
		userInfo.setVisible(signedIn);
		if (user != null) {
			username.setText(user.getFirstname()+" " + user.getLastname());
		}
	}
}
