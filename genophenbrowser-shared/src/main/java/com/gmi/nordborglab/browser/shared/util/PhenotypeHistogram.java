package com.gmi.nordborglab.browser.shared.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/19/13
 * Time: 10:52 PM
 * Class to calculate histogram
 */
public class PhenotypeHistogram {


    public static ImmutableSortedMap<Double,Integer> getHistogram(List<Double> values,int binCount) {
        ImmutableSortedMap<Double,Integer> histogram;
        SortedMultiset<Double> data = TreeMultiset.create();
        data.clear();
        for (Double value : values) {
            if (value != null)
                data.add(value);
        }
        if (data.size() == 0)
            return null;
        Double min = data.elementSet().first();
        Double max = data.elementSet().last();
        if (min.equals(max))
            return null;
        Double binWidth = (max - min) / binCount;
        ImmutableSortedMap.Builder<Double, Integer> builder = ImmutableSortedMap
                .naturalOrder();
        for (int i = 0; i < binCount; i++) {
            Double lowBound = min + i * binWidth;
            Double upperBound = lowBound + binWidth;
            builder.put(
                    lowBound,
                    data.subMultiset(lowBound, BoundType.CLOSED,
                            upperBound, BoundType.CLOSED).size());
        }
        builder.put(max, 0);
        histogram = builder.build();
        return histogram;
    }
}
