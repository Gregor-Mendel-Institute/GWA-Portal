package com.gmi.nordborglab.browser.client.mvp.presenter.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class UserInfoPresenter extends
		PresenterWidget<UserInfoPresenter.MyView> {

	public interface MyView extends View {
		String getUserdata();
		void showUserInfo(AppUserProxy user);
	}
	
	private AppUserProxy user;
	private final AppUserFactory appUserFactory;
	
	@Inject
	public UserInfoPresenter(final EventBus eventBus, final MyView view,final AppUserFactory appUserFactory) {
		super(eventBus, view);
		this.appUserFactory = appUserFactory;
	}

	@Override
	protected void onBind() {
		super.onBind();
		String userData = getView().getUserdata();
		if (userData != null) {
			try {
				AutoBean<AppUserProxy> userBean = AutoBeanCodex.decode(appUserFactory, AppUserProxy.class, userData);
				user = userBean.as();
			}
			catch (Exception e ) {
				Logger logger = Logger.getLogger("");
				logger.log(Level.SEVERE, "Autobean decoding", e);
			}
		}
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		getView().showUserInfo(user);
	}
}
