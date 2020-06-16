package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StockServiceTest extends BaseTest {

    @Resource
    private StockService service;

    @Before
    public void setUp() {
    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }

    @Test
    public void testFindOne() {
        Stock stock = service.findOne(1L);
        assertNotNull("No stock found", stock);
        assertEquals("wrong stock found", 1, stock.getId().longValue());
        assertNotNull("no pedigree data", stock.getPedigreeData());
    }

//	@Test
//	public void testFindAll() {
//		List<Stock> stocks = service.findAll(1L);
//		assertNotNull("No stocks found",stocks);
//		assertEquals("wrong number of stocks",1,stocks.size());
//		Stock stock = stocks.get(0);
//		assertEquals("wrong associated passport",1,stock.getPassport().getId().longValue());
//	}
}