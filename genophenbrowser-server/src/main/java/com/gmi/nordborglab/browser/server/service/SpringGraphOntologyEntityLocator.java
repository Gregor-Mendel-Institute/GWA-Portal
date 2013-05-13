package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.ontology.BaseGraphOntologyEntity;
import com.gmi.nordborglab.jpaontology.model.BaseOntologyEntity;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.Locator;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 5:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpringGraphOntologyEntityLocator extends Locator<BaseGraphOntologyEntity,Long> {



    public SpringGraphOntologyEntityLocator() {

    }

    @Override
    public BaseGraphOntologyEntity create(Class<? extends BaseGraphOntologyEntity> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BaseGraphOntologyEntity find(Class<? extends BaseGraphOntologyEntity> clazz, Long id) {
        return null;
    }

    @Override
    public Class<BaseGraphOntologyEntity> getDomainType() {
        return null;
    }

    @Override
    public Long getId(BaseGraphOntologyEntity domainObject) {
        return (long)domainObject.getNodeId();
    }

    @Override
    public Class<Long> getIdType() {
        return Long.class;
    }

    @Override
    public Object getVersion(BaseGraphOntologyEntity domainObject) {
        return 0;
    }



    @Override
    public boolean isLive(BaseGraphOntologyEntity domainObject) {
        return true;
    }

}