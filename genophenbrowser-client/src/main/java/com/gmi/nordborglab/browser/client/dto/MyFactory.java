package com.gmi.nordborglab.browser.client.dto;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

public interface MyFactory extends AutoBeanFactory {
	AutoBean<UserInfo> userInfo();
    AutoBean<PhenotypeUploadDataProxy> phenotypeUploadData();
}
