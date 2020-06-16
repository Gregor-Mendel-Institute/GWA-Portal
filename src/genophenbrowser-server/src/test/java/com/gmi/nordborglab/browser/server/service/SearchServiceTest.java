package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SearchServiceTest extends BaseTest {

    @Resource
    private SearchService service;

    @Before
    public void setUp() {


    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }


    @Test
    public void testSearchDiverityByTerm() {
        List<SearchFacetPage> searchResults = service.searchByTerm("flowering", CATEGORY.DIVERSITY, null);
        assertNotNull(searchResults);
        assertEquals(3, searchResults.size());
    }

    @Test
    public void testSearchGermplasmyByTerm() {
        List<SearchFacetPage> searchResults = service.searchByTerm("Sha", CATEGORY.GERMPLASM, null);
        assertNotNull(searchResults);
        assertEquals(1, searchResults.size());
    }

    @Test
    public void testSearchGermplasmyByIdTerm() {
        List<SearchFacetPage> searchResults = service.searchByTerm("5094", CATEGORY.GERMPLASM, null);
        assertNotNull(searchResults);
        assertEquals(2, searchResults.size());
    }

    @Test
    public void testSearchGeneByTerm() {
        SearchFacetPage searchResult = service.searchGeneByTerm("AT1G12");
        assertNotNull(searchResult);
        assertEquals(5, searchResult.getContents().size());
        assertEquals(120, searchResult.getTotal());
    }


}
