package com.gmi.nordborglab.browser.server.service;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;

public class SpringServiceLocator implements ServiceLocator{

	public Object getInstance(Class<?> clazz) {
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(RequestFactoryServlet.getThreadLocalServletContext());
		return context.getBean(clazz);
	}

}
