package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.requestfactory.shared.impl.EntityProxyCategory;

@AutoBeanFactory.Category(value=EntityProxyCategory.class) 
public interface HelperFactory extends AutoBeanFactory {

	AutoBean<AppDataProxy> appData();
	AutoBean<UnitOfMeasureProxy> unitOfMeasure();
	AutoBean<StatisticTypeProxy> statisticType();
}
