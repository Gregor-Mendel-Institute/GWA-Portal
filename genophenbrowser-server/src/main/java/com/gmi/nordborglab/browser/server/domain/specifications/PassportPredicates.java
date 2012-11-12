package com.gmi.nordborglab.browser.server.domain.specifications;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.genotype.QAlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.QPassport;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Predicate;

public class PassportPredicates {
	
	public static Predicate taxonomyIdEqual(final Long taxonomyId) {
		QPassport passport = QPassport.passport;
		return passport.taxonomy.id.eq(taxonomyId);
	}
	
	public static Predicate passportIdEqual(final Long passportId) {
		if (passportId == null)
			return null;
		QPassport passport = QPassport.passport;
		return passport.id.eq(passportId);
	}
	
	public static Predicate sampStatIdEqual(final Long sampStatId) {
		if (sampStatId == null)
			return null;
		QPassport passport = QPassport.passport;
		return passport.sampstat.id.eq(sampStatId);
	}
	
	public static Predicate accNameContains(final String accName) {
		if (accName == null || accName.equals(""))
			return null;
		QPassport passport = QPassport.passport;
		return passport.accename.containsIgnoreCase(accName);
	}
	
	public static Predicate collectorContains(final String collector) {
		if (collector == null || collector.equals(""))
			return null;
		QPassport passport = QPassport.passport;
		return passport.collection.collector.containsIgnoreCase(collector);
	}
	
	public static Predicate accNumberContains(final String accNumber) {
		if (accNumber == null || accNumber.equals(""))
			return null;
		QPassport passport = QPassport.passport;
		return passport.accenumb.containsIgnoreCase(accNumber);
	}
	
	public static Predicate sourceContains(final String source) {
		if (source == null || source.equals(""))
			return null;
		QPassport passport = QPassport.passport;
		return passport.source.source.containsIgnoreCase(source);
	}
	
	public static Predicate alleleAssayIdsEqual(final List<Long> alleleAssayIds) {
		BooleanBuilder predicate = new BooleanBuilder();
		for (Long alleleAssayId:alleleAssayIds) {
			predicate.and(alleleAssayIdEqual(alleleAssayId));
		}
		return predicate;
	}
	
	
	public static Predicate alleleAssayIdEqual(final Long alleleAssayId) {
		if (alleleAssayId == null)
			return null;
		QPassport passport = QPassport.passport;
		return passport.alleles.any().alleleAssay.id.eq(alleleAssayId);
	}
	
	public static Predicate countryIn(final String country) {
		if (country == null || country.equals(""))
			return null;
		QPassport passport = QPassport.passport;
		return passport.collection.locality.origcty.equalsIgnoreCase(country);
	}
	
	public static Predicate[] countriesIn(final List<String> countries) {
		Predicate[] predicates = new Predicate[countries.size()];
		for (int i = 0;i<countries.size();i++) {
			predicates[i] = countryIn(countries.get(i));
		}
		return predicates;
	}

}
