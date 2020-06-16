package com.gmi.nordborglab.browser.server.math;

import com.gmi.nordborglab.browser.shared.proxy.TransformationDataProxy;
import com.gmi.nordborglab.browser.shared.util.Normality;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import flanagan.analysis.BoxCox;
import flanagan.analysis.Stat;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.notNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Transformations {

    abstract static class TransformFunc implements Function<Double, Double> {

        protected final Double minValue;

        protected final Double variance;

        public TransformFunc(final Double minValue, final Double variance) {
            this.minValue = minValue;
            this.variance = variance;
        }
    }

    static class RawTransformFunc extends TransformFunc {
        public RawTransformFunc(Double minValue, Double stdVariance) {
            super(minValue, stdVariance);
        }


        @Nullable
        @Override
        public Double apply(@Nullable Double input) {
            if (input == null)
                return null;
            return input;
        }
    }

    public static class LogTransformFunc extends TransformFunc {

        public LogTransformFunc(Double minValue, Double stdVariance) {
            super(minValue, stdVariance);
        }


        @Nullable
        @Override
        public Double apply(@Nullable Double input) {
            if (input == null)
                return null;
            return Math.log(input - minValue + variance * 0.1);
        }

    }

    static class SqrtTransformFunc extends TransformFunc {

        public SqrtTransformFunc(Double minValue, Double stdVariance) {
            super(minValue, stdVariance);
        }

        @Nullable
        @Override
        public Double apply(@Nullable Double input) {
            if (input == null)
                return null;
            return Math.sqrt(input - minValue + variance * 0.1);
        }

    }

    static class BoxCoxTransformFunc extends TransformFunc {

        public BoxCoxTransformFunc(Double minValue, Double stdVariance) {
            super(minValue, stdVariance);
        }

        @Nullable
        @Override
        public Double apply(@Nullable Double input) {
            if (input == null)
                return null;
            return input - minValue + variance * 0.1;
        }

    }

    static DescriptiveStatistics getDescriptiveStatistics(Collection<Double> values) {
        DescriptiveStatistics stats = new DescriptiveStatistics(Doubles.toArray(Collections2.filter(values, notNull())));
        return stats;
    }

    public static List<Double> logTransform(List<Double> values) {
        DescriptiveStatistics stats = getDescriptiveStatistics(values);
        Double variance = stats.getVariance();
        Double min = stats.getMin();
        return Lists.transform(values, new LogTransformFunc(min, variance));
    }


    public static List<Double> sqrtTransform(List<Double> values) {
        DescriptiveStatistics stats = getDescriptiveStatistics(values);
        Double variance = stats.getVariance();
        Double min = stats.getMin();
        return Lists.transform(values, new SqrtTransformFunc(min, variance));
    }

    public static List<Double> boxCoxTransform(List<Double> values) {
        DescriptiveStatistics stats = getDescriptiveStatistics(values);
        Double variance = stats.getVariance();
        Double min = stats.getMin();
        values = Lists.transform(values, new BoxCoxTransformFunc(min, variance));
        Stat stat = new Stat(values.toArray(new Double[]{}));
        BoxCox boxCox = new BoxCox(stat);
        double[] transformedValues = boxCox.transformedData();
        return Doubles.asList(transformedValues);
    }

    public static Double calculateShapiroPval(List<Double> values) {
        Normality normality = new Normality(values);
        return normality.getShapiroWilkPvalue();
    }

    public static List<Double> transform(TransformationDataProxy.TYPE transformation, List<Double> values) {
        switch (transformation) {
            case LOG:
                return logTransform(values);
            case SQRT:
                return sqrtTransform(values);
            case BOXCOX:
                return boxCoxTransform(values);
            default:
                return values;
        }
    }
}
