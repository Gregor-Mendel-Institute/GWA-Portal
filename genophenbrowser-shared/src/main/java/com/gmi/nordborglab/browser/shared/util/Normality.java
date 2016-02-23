package com.gmi.nordborglab.browser.shared.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by uemit.seren on 2/17/16.
 */
public class Normality {

    private final double[] values;
    private final double[] pvalue = new double[]{0};
    private final double[] wilk = new double[]{0};
    private final double a[];
    private final int n;
    private final int n2;
    private final int[] error = new int[]{0};
    private boolean calculated = false;

    public Normality(List<Double> vals) {
        n = vals.size();
        values = new double[n + 1];
        n2 = Math.round(n / 2);
        for (int i = 0; i < vals.size(); i++) {
            values[i + 1] = vals.get(i);
        }
        a = new double[values.length];
        Arrays.sort(values);
    }

    public double getShapiroWilkPvalue() {
        this.calculate();
        return pvalue[0];
    }

    public double getShapiroWilkStat() {
        this.calculate();
        return wilk[0];
    }

    private void calculate() {
        if (this.calculated)
            return;
        SWilk.swilk(new boolean[]{false}, values, n, n, n2, a, wilk, pvalue, error);
        if (error[0] != 0) {
            throw new RuntimeException("Error calculating shapiro wilk score" + error[0]);
        }
        this.calculated = true;
    }

    public static Double getShapiroWilkPvalue(List<Double> values) {
        return new Normality(values).getShapiroWilkPvalue();
    }

    public static Double calculateScoreFromPValue(Double pValue) {
        if (pValue == null)
            return null;
        if (pValue > 0.0) {
            return -Math.log10(pValue);
        }
        return Double.NaN;
    }

    public static Double getRoundedValue(Double value) {
        if (value == null || Double.isNaN(value))
            return value;
        return Math.round(value * 100) / 100.0;
    }
}
