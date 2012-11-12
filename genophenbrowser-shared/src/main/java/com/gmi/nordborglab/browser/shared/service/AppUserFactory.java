package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

public interface AppUserFactory extends AutoBeanFactory{
	AutoBean<AppUserProxy> appuser();
	AutoBean<AuthorityProxy> authority();
	AutoBean<AppUserProxy> appuser(AppUserProxy appUser);
}
