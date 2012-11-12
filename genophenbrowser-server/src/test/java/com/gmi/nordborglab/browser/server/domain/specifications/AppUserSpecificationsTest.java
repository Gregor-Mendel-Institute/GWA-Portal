package com.gmi.nordborglab.browser.server.domain.specifications;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser_;

public class AppUserSpecificationsTest {

	private static final String SEARCH_TERM = "Foo";
	private static final String SEARCH_TERM_LIKE_PATTERN = "foo%";
	
	private CriteriaBuilder criteriaBuilderMock;
    
    private CriteriaQuery criteriaQueryMock;
    
    private Root<AppUser> appUserRootMock;
	
	@Before
    public void setUp() {
       criteriaBuilderMock = mock(CriteriaBuilder.class);
       criteriaQueryMock = mock(CriteriaQuery.class);
       appUserRootMock = mock(Root.class);
    }
	
	
	@Test
	public void testLastNameIsLike() {
		Path lastNamePathMock = mock(Path.class);        
        when(appUserRootMock.get(AppUser_.lastname)).thenReturn(lastNamePathMock);
        
        Expression lastNameToLowerExpressionMock = mock(Expression.class);
        when(criteriaBuilderMock.lower(lastNamePathMock)).thenReturn(lastNameToLowerExpressionMock);
        
        Predicate lastNameIsLikePredicateMock = mock(Predicate.class);
        when(criteriaBuilderMock.like(lastNameToLowerExpressionMock, SEARCH_TERM_LIKE_PATTERN)).thenReturn(lastNameIsLikePredicateMock);

        Specification<AppUser> actual = AppUserSpecifications.lastNameIsLike(SEARCH_TERM);
        Predicate actualPredicate = actual.toPredicate(appUserRootMock, criteriaQueryMock, criteriaBuilderMock);
        
        verify(appUserRootMock, times(1)).get(AppUser_.lastname);
        verifyNoMoreInteractions(appUserRootMock);
        
        verify(criteriaBuilderMock, times(1)).lower(lastNamePathMock);
        verify(criteriaBuilderMock, times(1)).like(lastNameToLowerExpressionMock, SEARCH_TERM_LIKE_PATTERN);
        verifyNoMoreInteractions(criteriaBuilderMock);

        verifyZeroInteractions(criteriaQueryMock, lastNamePathMock, lastNameIsLikePredicateMock);

        assertEquals(lastNameIsLikePredicateMock, actualPredicate);
	}
	
	@Test
	public void testFirstNameIsLike() {
		Path firstNamePathMock = mock(Path.class);        
        when(appUserRootMock.get(AppUser_.firstname)).thenReturn(firstNamePathMock);
        
        Expression firstNameToLowerExpressionMock = mock(Expression.class);
        when(criteriaBuilderMock.lower(firstNamePathMock)).thenReturn(firstNameToLowerExpressionMock);
        
        Predicate firstNameIsLikePredicateMock = mock(Predicate.class);
        when(criteriaBuilderMock.like(firstNameToLowerExpressionMock, SEARCH_TERM_LIKE_PATTERN)).thenReturn(firstNameIsLikePredicateMock);

        Specification<AppUser> actual = AppUserSpecifications.firstNameIsLike(SEARCH_TERM);
        Predicate actualPredicate = actual.toPredicate(appUserRootMock, criteriaQueryMock, criteriaBuilderMock);
        
        verify(appUserRootMock, times(1)).get(AppUser_.firstname);
        verifyNoMoreInteractions(appUserRootMock);
        
        verify(criteriaBuilderMock, times(1)).lower(firstNamePathMock);
        verify(criteriaBuilderMock, times(1)).like(firstNameToLowerExpressionMock, SEARCH_TERM_LIKE_PATTERN);
        verifyNoMoreInteractions(criteriaBuilderMock);

        verifyZeroInteractions(criteriaQueryMock, firstNamePathMock, firstNameIsLikePredicateMock);

        assertEquals(firstNameIsLikePredicateMock, actualPredicate);
	}

	
}
