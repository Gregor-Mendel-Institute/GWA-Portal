package com.gmi.nordborglab.browser.server.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.Locator;


public class SpringEntitiyLocator extends Locator<BaseEntity,Long>{
	
	@PersistenceContext
	private EntityManager em;

	
	public SpringEntitiyLocator() {
		
	}

	@Override
	public BaseEntity create(Class<? extends BaseEntity> clazz) {
		 try {
		      return clazz.newInstance();
		    } catch (InstantiationException e) {
		      throw new RuntimeException(e);
		    } catch (IllegalAccessException e) {
		      throw new RuntimeException(e);
		    }
	}

	@Override
	public BaseEntity find(Class<? extends BaseEntity> clazz, Long id) {
		return getEM().find(clazz, id);
	}

	@Override
	public Class<BaseEntity> getDomainType() {
		return null;
	}

	@Override
	public Long getId(BaseEntity domainObject) {
		return domainObject.getId();
	}

	@Override
	public Class<Long> getIdType() {
		return Long.class;
	}

	@Override
	public Object getVersion(BaseEntity domainObject) {
		return domainObject.getId();
	}
	
	
	private EntityManager getEM() {
		if (em == null) {
			ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(RequestFactoryServlet.getThreadLocalServletContext());
			SharedEntityManagerBean bean =  context.getBean(SharedEntityManagerBean.class);
			em = bean.getObject();
		}
		return em;
	}

	@Override
	public boolean isLive(BaseEntity domainObject) {
		// TODO Auto-generated method stub
		return super.isLive(domainObject);
	}
	
}
