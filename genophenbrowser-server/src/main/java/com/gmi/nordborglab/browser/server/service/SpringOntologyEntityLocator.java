package com.gmi.nordborglab.browser.server.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.jpaontology.model.BaseOntologyEntity;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.Locator;

public class SpringOntologyEntityLocator extends Locator<BaseOntologyEntity, Integer> {

    @PersistenceContext
    private EntityManager em;


    public SpringOntologyEntityLocator() {

    }

    @Override
    public BaseOntologyEntity create(Class<? extends BaseOntologyEntity> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BaseOntologyEntity find(Class<? extends BaseOntologyEntity> clazz, Integer id) {
        return getEM().find(clazz, id);
    }

    @Override
    public Class<BaseOntologyEntity> getDomainType() {
        return null;
    }

    @Override
    public Integer getId(BaseOntologyEntity domainObject) {
        return domainObject.getId();
    }

    @Override
    public Class<Integer> getIdType() {
        return Integer.class;
    }

    @Override
    public Object getVersion(BaseOntologyEntity domainObject) {
        return domainObject.getId();
    }


    private EntityManager getEM() {
        if (em == null) {
            ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(RequestFactoryServlet.getThreadLocalServletContext());
            em = context.getBean("entityManagerOntology", EntityManager.class);
        }
        return em;
    }

    @Override
    public boolean isLive(BaseOntologyEntity domainObject) {
        return true;
    }

}