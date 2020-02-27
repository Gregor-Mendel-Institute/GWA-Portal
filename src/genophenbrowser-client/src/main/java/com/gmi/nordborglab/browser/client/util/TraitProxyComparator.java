package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

public class TraitProxyComparator extends  Ordering<TraitProxy> {

	@Override
	public int compare(TraitProxy o1, TraitProxy o2) {
		Double value1 = Double.MIN_VALUE;
		Double value2 = Double.MAX_VALUE;
		try {
			if (o1.getValue() != null)
				value1 = Double.parseDouble(o1.getValue());
		}
		catch (NumberFormatException e) {
		}
		try {
			if (o2.getValue() != null)
				value2 = Double.parseDouble(o2.getValue());
		} catch (NumberFormatException e) {
			
		}
		return Doubles.compare(value1, value2);
	}

}
