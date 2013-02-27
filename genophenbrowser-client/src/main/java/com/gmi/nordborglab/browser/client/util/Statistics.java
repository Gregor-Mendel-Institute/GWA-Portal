package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.AlleleProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/23/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Statistics {


    public static Function<TraitProxy, Double> traitToDouble = new Function<TraitProxy, Double>() {
        @Nullable
        @Override
        public Double apply(@Nullable TraitProxy input) {
            try {
                return Double.parseDouble(input.getValue());
            } catch (Exception e) {
            }
            return null;
        }
    };


    public static ImmutableMultiset getGeoChartData(List<TraitProxy> traitValues) {
        ImmutableMultiset.Builder<String> builder = ImmutableMultiset.builder();
        for (TraitProxy trait : traitValues) {
            try {
                String cty = trait.getObsUnit().getStock().getPassport()
                        .getCollection().getLocality().getCountry();
                builder.add(cty);
            } catch (NullPointerException e) {

            }
        }
        return builder.build();
    }


}
