package com.gmi.nordborglab.browser.server.testutils;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:META-INF/applicationContext.xml",
		"classpath:META-INF/spring-security.xml",
		"classpath:META-INF/spring-acl.xml"})
@TransactionConfiguration
@Transactional
public abstract class BaseTest {

}
