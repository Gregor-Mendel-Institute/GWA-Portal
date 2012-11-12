package com.gmi.nordborglab.browser.client.entrypoints;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.gin.ClientGinjector;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.gmi.nordborglab.browser.shared.service.HelperFactory;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

public class Browser implements EntryPoint {
	
	private final ClientGinjector ginjector = GWT.create(ClientGinjector.class);

	public void onModuleLoad() {
		DelayedBindRegistry.bind(ginjector);
		AppUserFactory appUserFactory = ginjector.getAppUserFactory();
		HelperFactory helperFactory = ginjector.getHelperFactory();
		CurrentUser currentUser = ginjector.getCurrentUser();
		/*String appData = getAppData();
		if (appData != null) {
			try {
				AutoBean<AppDataProxy> appDataBean = AutoBeanCodex.decode(helperFactory,AppDataProxy.class, appData);
				currentUser.setAppData(appDataBean.as());
			}
			catch (Exception e ) {
				Logger logger = Logger.getLogger("");
				logger.log(Level.SEVERE, "Autobean decoding", e);
			}
		}*/
		
		String userData = getUserData();
		if (userData != null) {
			try {
				AutoBean<AppUserProxy> userBean = AutoBeanCodex.decode(appUserFactory, AppUserProxy.class, userData);
				currentUser.setAppUser(userBean.as());
			}
			catch (Exception e ) {
				Logger logger = Logger.getLogger("");
				logger.log(Level.SEVERE, "Autobean decoding", e);
			}
		}
		ginjector.getPlaceManager().revealCurrentPlace();
		MainResources mainResources = ginjector.getMainResources();
		mainResources.style().ensureInjected();
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			
			@Override
			public void onUncaughtException(Throwable e) {
				Logger logger = Logger.getLogger("uncaught");
				logger.log(Level.SEVERE, "Uncaught Exception", e);
			}
		});
	}

	private String getAppData() {
		String appData = null;
		try {
			Dictionary data = Dictionary.getDictionary("appData");
			if (data != null) {
				appData =  data.get("data");
			}
		}
		catch (Exception e) {}
		return appData;
	}

	protected String getUserData() {
		String user = null;
		try {
			Dictionary data = Dictionary.getDictionary("userData");
			if (data != null) {
				user =  data.get("user");
			}
		}
		catch (Exception e) {}
		return user;
	}
}
