package com.gmi.nordborglab.browser.server.domain.specifications;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.mysema.query.types.Predicate;

public class PassportPredicatesTest {
	
	

    @Test
    public void testTaxonomyIdEqual() {
        Predicate predicate = PassportPredicates.taxonomyIdEqual(1L);
        String predicateAsString = predicate.toString();
        assertEquals("passport.taxonomy.id = 1", predicateAsString);
    }
    
    @Test
    public void testPassportIdEqual() {
        Predicate predicate = PassportPredicates.passportIdEqual(1L);
        String predicateAsString = predicate.toString();
        assertEquals("passport.id = 1", predicateAsString);
    }
}
