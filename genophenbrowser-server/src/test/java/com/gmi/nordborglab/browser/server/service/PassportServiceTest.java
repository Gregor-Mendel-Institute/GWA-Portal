package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.PassportSearchCriteria;
import com.gmi.nordborglab.browser.server.domain.PassportStats;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.pages.PassportPage;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;

public class PassportServiceTest extends BaseTest {

	@Resource
	private PassportService service;
	
	@Before
	public void setUp() {
	}

	@After
	public void clearContext() {
		SecurityUtils.clearContext();
	}

	@Test
	public void testFindAll() {
		PassportSearchCriteria filter = new PassportSearchCriteria();
		PassportPage page = service.findAll(1L,filter, 0, 50);
		assertEquals(7064, page.getTotalElements());
		assertEquals(50, page.getNumberOfElements());
		assertEquals(50, page.getContent().size());
	}
	
	@Test
	public void testFindAllByPassportId() {
		PassportSearchCriteria filter = new PassportSearchCriteria();
		filter.setPassportId(1L);
		PassportPage page = service.findAll(1L,filter, 0, 50);
		assertEquals(1, page.getTotalElements());
		assertEquals(1, page.getNumberOfElements());
		assertEquals(1, page.getContent().size());
	}
	
	@Test
	public void testFindAllByCountry() {
		PassportSearchCriteria filter = new PassportSearchCriteria();
		List<String> countries = new ArrayList<String>();
		countries.add("FRA");
		filter.setCountries(countries);
		PassportPage page = service.findAll(1L,filter, 0, 50);
		assertEquals(444, page.getTotalElements());
		assertEquals(50, page.getNumberOfElements());
		assertEquals(50, page.getContent().size());
	}
	
	@Test
	public void testFindAllByCountries() {
		PassportSearchCriteria filter = new PassportSearchCriteria();
		List<String> countries = new ArrayList<String>();
		countries.add("FRA");
		countries.add("GER");
		filter.setCountries(countries);
		PassportPage page = service.findAll(1L,filter, 0, 50);
		assertEquals(712, page.getTotalElements());
		assertEquals(50, page.getNumberOfElements());
		assertEquals(50, page.getContent().size());
	}
	
	@Test
	public void testFindOne() {
		Passport passport = service.findOne(1L);
		assertNotNull("no passport found",passport);
		assertEquals("id wrong",1,passport.getId().longValue());
	}
	
	@Test
	public void testFindAllStocks() {
		List<Stock> stocks = service.findAllStocks(1L);
		assertNotNull("No stocks found",stocks);
		assertEquals("wrong number of stocks",1,stocks.size());
		Stock stock = stocks.get(0);
		assertEquals("wrong associated passport",1,stock.getPassport().getId().longValue());
	}
	
	@Test
	public void testFindStats() {
		PassportStats stats = service.findStats(1L);
		assertNotNull(stats);
		assertNotNull(stats.getData());
	}
}
