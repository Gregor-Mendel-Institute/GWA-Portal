package com.gmi.nordborglab.browser.server.domain.specifications;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser_;


public class AppUserSpecifications {

	public static Specification<AppUser> lastNameIsLike(final String searchTerm) {
		return new Specification<AppUser>() {

			@Override
			public Predicate toPredicate(Root<AppUser> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				String likePattern = getLikePattern(searchTerm);
				return cb.like(cb.lower(root.<String>get(AppUser_.lastname)), likePattern);
			}
		};
	}
	
	public static Specification<AppUser> firstNameIsLike(final String searchTerm) {
		return new Specification<AppUser>() {

			@Override
			public Predicate toPredicate(Root<AppUser> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				String likePattern = getLikePattern(searchTerm);
				return cb.like(cb.lower(root.<String>get(AppUser_.firstname)), likePattern);
			}
		};
	}
	
	public static String getLikePattern(final String searchTerm) {
		StringBuilder pattern = new StringBuilder();
		pattern.append(searchTerm.toLowerCase());
		pattern.append("%");
		return pattern.toString();
	}
}
