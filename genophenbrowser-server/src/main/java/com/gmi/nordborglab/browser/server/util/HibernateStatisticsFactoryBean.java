package com.gmi.nordborglab.browser.server.util;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;

/**
 * Created by uemit.seren on 7/29/14.
 */
@Component
public class HibernateStatisticsFactoryBean implements FactoryBean<Statistics> {

    @Resource
    private EntityManagerFactory entityManagerFactory;

    @Override
    public Statistics getObject() throws Exception {
        SessionFactory sessionFactory = ((HibernateEntityManagerFactory) entityManagerFactory).getSessionFactory();
        return sessionFactory.getStatistics();
    }

    @Override
    public Class<?> getObjectType() {
        return Statistics.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}