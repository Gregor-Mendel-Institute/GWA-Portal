package com.gmi.nordborglab.browser.server.domain.specifications;

import com.gmi.nordborglab.browser.server.domain.phenotype.QTraitUom;
import com.mysema.query.types.Predicate;

public class TraitUomPredicates {

	public static Predicate localTraitNameContains(final String name) {
		if (name == null || name.equals(""))
			return null;
		QTraitUom traitUom = QTraitUom.traitUom;
		return traitUom.local_trait_name.containsIgnoreCase(name);
	}
}
