package com.gmi.nordborglab.browser.client.util;

import static com.google.common.base.Predicates.alwaysTrue;

import javax.annotation.Nullable;

import com.gmi.nordborglab.browser.shared.proxy.AccessionCollectionProxy;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampStatProxy;
import com.gmi.nordborglab.browser.shared.proxy.SourceProxy;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.regexp.shared.RegExp;

public class PassportProxyPredicates {
	
	
	public static Predicate<PassportProxy> idEquals(final Long id) {
		if (id == null)
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null || input.getId() == null)
					return false;
				return input.getId().equals(id);
			}
		};
	}
	
	
	public static Predicate<PassportProxy> accNameContains(final String accName) {
		if (accName == null || accName.equals(""))
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null || input.getAccename() == null)
					return false;
				RegExp regexp = RegExp.compile(accName,"i");
				return regexp.test(input.getAccename());
			}
		};
	}
	
	public static Predicate<PassportProxy> accNumberContains(final String accNumber) {
		if (accNumber == null || accNumber.equals(""))
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null || input.getAccenumb() == null)
					return false;
				return input.getAccenumb().contains(accNumber);
			}
		};
	}
	
	public static Predicate<PassportProxy> collectorContains(final String collector) {
		if (collector == null || collector.equals(""))
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null)
					return false;
				return collectorNameContains(collector).apply(input.getCollection());
			}
		};
	}
	
	public static Predicate<PassportProxy> sourceContains(final String source) {
		if (source == null || source.equals(""))
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null)
					return false;
				return sourceNameContains(source).apply(input.getSource());
			}
		};
	}
	
	public static Predicate<PassportProxy> countryEquals(final String country) {
		if (country == null || country.equals(""))
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null || input.getCollection() == null)
					return false;
				return countryNameEquals(country).apply(input.getCollection().getLocality());
			}
		};
	}
	
	public static Predicate<AccessionCollectionProxy> collectorNameContains(final String collector) {
		if (collector == null || collector.equals(""))
			return alwaysTrue();
		return new Predicate<AccessionCollectionProxy>() {

			@Override
			public boolean apply(@Nullable AccessionCollectionProxy input) {
				if (input == null || input.getCollector() == null)
					return false;
				return input.getCollector().contains(collector);
			}
		};
	}
	
	public static Predicate<SourceProxy> sourceNameContains(final String source) {
		if (source == null || source.equals(""))
			return alwaysTrue();
		return new Predicate<SourceProxy>() {

			@Override
			public boolean apply(@Nullable SourceProxy input) {
				if (input == null || input.getSource() == null)
					return false;
				return input.getSource().contains(source);
			}
		};
	}
	
	public static Predicate<LocalityProxy> countryNameEquals(final String country) {
		if (country == null || country.equals(""))
			return alwaysTrue();
		return new Predicate<LocalityProxy>() {

			@Override
			public boolean apply(@Nullable LocalityProxy input) {
				if (input == null || input.getOrigcty() == null)
					return false;
				return input.getOrigcty() == country;
			}
		};
	}
	
	public static Predicate<LocalityProxy> countryNameContains(final String country) {
		if (country == null || country.equals(""))
			return alwaysTrue();
		return new Predicate<LocalityProxy>() {

			@Override
			public boolean apply(@Nullable LocalityProxy input) {
				if (input == null || input.getOrigcty() == null)
					return false;
				RegExp regexp = RegExp.compile(country,"i");
				return regexp.test(input.getOrigcty());
			}
		};
	}


	public static Predicate<PassportProxy> countryContains(final String country) {
		if (country == null || country.equals(""))
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null || input.getCollection() == null)
					return false;
				return countryNameContains(country).apply(input.getCollection().getLocality());
			}
		};
	}
	
	public static Predicate<PassportProxy> alleleAssayIdEquals(final Long alleleAssayId) {
		if (alleleAssayId == null )
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null || input.getAlleleAssays() == null)
					return false;
				return Iterables.any(input.getAlleleAssays(), allelAssayEquals(alleleAssayId));
			}
		};
	}
	

	public static Predicate<AlleleAssayProxy> allelAssayEquals(final Long alleleAssayId) {
		if (alleleAssayId == null)
			return alwaysTrue();
		return new Predicate<AlleleAssayProxy>() {

			@Override
			public boolean apply(@Nullable AlleleAssayProxy input) {
				if (input == null)
					return false;
				return alleleAssayId == input.getId();
			}
		};
	}

	public static Predicate<PassportProxy> sampStatIdEquals(final Long sampStatId) {
		if (sampStatId == null)
			return alwaysTrue();
		return new Predicate<PassportProxy>() {

			@Override
			public boolean apply(@Nullable PassportProxy input) {
				if (input == null || input.getSampstat() == null)
					return false;
				return sampStatEquals(sampStatId).apply(input.getSampstat());
			}
		};
	}
	
	public static Predicate<SampStatProxy> sampStatEquals(final Long sampStatId) {
		if (sampStatId == null || sampStatId.equals(""))
			return alwaysTrue();
		return new Predicate<SampStatProxy>() {

			@Override
			public boolean apply(@Nullable SampStatProxy input) {
				if (input == null)
					return false;
				return sampStatId == input.getId();
			}
		};
	}
	
}
