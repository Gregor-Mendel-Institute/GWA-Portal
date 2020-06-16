package com.gmi.nordborglab.browser.shared.util;

/**
 * Calculates the Shapiro-Wilk W test and its significance level.
 * <p/>
 * Ported from original FORTRAN 77 code from the journal Applied Statistics published by the Royal Statistical Society and
 * distributed by Carnegie Mellon University at http://lib.stat.cmu.edu/apstat/.
 * <p/>
 * To help facilitate debugging and maintenance, this port has been changed as little as feasible from the original FORTRAN 77
 * code, to allow comparisons with the original. Variable names have been left alone when possible (except for capitalizing
 * constants), and the logic flow (though translated and indented) is essentially unchanged.
 * <p/>
 * The original FORTRAN source for these routines has been released by the Royal Statistical Society for free public distribution,
 * and this Java implementation is released to the public domain.
 * <p/>
 * This java implementation is part of the limewire project.
 */
public class SWilk {
    /*
     * Constants and polynomial coefficients for swilk(). NOTE: FORTRAN counts the elements of the array x[length] as
     * x[1] through x[length], not x[0] through x[length-1]. To avoid making pervasive, subtle changes to the algorithm
     * (which would inevitably introduce pervasive, subtle bugs) the referenced arrays are padded with an unused 0th
     * element, and the algorithm is ported so as to continue accessing from [1] through [length].
     */
    private static final double C1[] = {Double.NaN, 0.0E0, 0.221157E0, -0.147981E0, -0.207119E1, 0.4434685E1, -0.2706056E1};
    private static final double C2[] = {Double.NaN, 0.0E0, 0.42981E-1, -0.293762E0, -0.1752461E1, 0.5682633E1, -0.3582633E1};
    private static final double C3[] = {Double.NaN, 0.5440E0, -0.39978E0, 0.25054E-1, -0.6714E-3};
    private static final double C4[] = {Double.NaN, 0.13822E1, -0.77857E0, 0.62767E-1, -0.20322E-2};
    private static final double C5[] = {Double.NaN, -0.15861E1, -0.31082E0, -0.83751E-1, 0.38915E-2};
    private static final double C6[] = {Double.NaN, -0.4803E0, -0.82676E-1, 0.30302E-2};
    private static final double C7[] = {Double.NaN, 0.164E0, 0.533E0};
    private static final double C8[] = {Double.NaN, 0.1736E0, 0.315E0};
    private static final double C9[] = {Double.NaN, 0.256E0, -0.635E-2};
    private static final double G[] = {Double.NaN, -0.2273E1, 0.459E0};
    private static final double Z90 = 0.12816E1, Z95 = 0.16449E1, Z99 = 0.23263E1;
    private static final double ZM = 0.17509E1, ZSS = 0.56268E0;
    private static final double BF1 = 0.8378E0, XX90 = 0.556E0, XX95 = 0.622E0;
    private static final double SQRTH = 0.70711E0, TH = 0.375E0, SMALL = 1E-19;
    private static final double PI6 = 0.1909859E1, STQR = 0.1047198E1;
    private static final boolean UPPER = true;


    /**
     * ALGORITHM AS R94 APPL. STATIST. (1995) VOL.44, NO.4
     * <p/>
     * Calculates Shapiro-Wilk normality test and P-value for sample sizes 3 <= n <= 5000 . Handles censored or uncensored data.
     * Corrects AS 181, which was found to be inaccurate for n > 50.
     * <p/>
     * NOTE: Semi-strange porting kludge alert. FORTRAN allows subroutine arguments to be modified by the called routine (passed by
     * reference, not value), and the original code for this routine makes use of that feature to return multiple results. To avoid
     * changing the code any more than necessary, I've used Java arrays to simulate this pass-by-reference feature. Specifically,
     * in the original code w, pw, and ifault are output results, not input parameters. Pass in double[1] arrays for w and pw, and
     * an int[1] array for ifault, and extract the computed values from the [0] element on return. The argument init is both input
     * and output; use a boolean[1] array and initialize [0] to false before the first call. The routine will update the value to
     * true to record that initialization has been performed, to speed up subsequent calls on the same data set. Note that although
     * the contents of a[] will be computed by the routine on the first call, the caller must still allocate the array space and
     * pass the unfilled array in to the subroutine. The routine will set the contents but not allocate the space.
     * <p/>
     * As described above with the constants, the data arrays x[] and a[] are referenced with a base element of 1 (like FORTRAN)
     * instead of 0 (like Java) to avoid screwing up the algorithm. To pass in 100 data points, declare x[101] and fill elements
     * x[1] through x[100] with data. x[0] will be ignored.
     * <p/>
     * You might want to eliminate the ifault parameter completely, and throw Java exceptions instead. I didn't want to change the
     * code that much.
     *
     * @param init   Input & output; pass in boolean[1], initialize to false before first call, routine will set to true
     * @param x      Input; Data set to analyze; 100 points go in x[101] array from x[1] through x[100]
     * @param n      Input; Number of data points in x
     * @param n1     Input; dunno
     * @param n2     Input; dunno either
     * @param a      Output when init[0] == false, Input when init[0] == true; holds computed test coefficients
     * @param w      Output; pass in double[1], will contain result in w[0] on return
     * @param pw     Output; pass in double[1], will contain result in pw[0] on return
     * @param ifault Output; pass in int[1], will contain error code (0 == good) in ifault[0] on return
     */
    public static void swilk(final boolean[] init,
                             final double[] x,
                             final int n,
                             final int n1,
                             final int n2,
                             final double[] a,
                             final double[] w,
                             final double[] pw,
                             final int[] ifault) {

        pw[0] = 1.0;
        if (w[0] >= 0.0) {
            w[0] = 1.0;
        }
        final double an = n;
        ifault[0] = 3;
        final int nn2 = n / 2;
        if (n2 < nn2) {
            return;
        }
        ifault[0] = 1;
        if (n < 3) {
            return;
        }

        // If INIT is false, calculates coefficients for the test

        if (!init[0]) {
            if (n == 3) {
                a[1] = SQRTH;
            } else {
                final double an25 = an + 0.25;
                double summ2 = 0.0;
                for (int i = 1; i <= n2; ++i) {
                    a[i] = ppnd((i - TH) / an25);
                    summ2 += a[i] * a[i];
                }
                summ2 *= 2.0;
                final double ssumm2 = Math.sqrt(summ2);
                final double rsn = 1.0 / Math.sqrt(an);
                final double a1 = poly(C1, 6, rsn) - a[1] / ssumm2;

                // Normalize coefficients

                int i1;
                double fac;
                if (n > 5) {
                    i1 = 3;
                    final double a2 = -a[2] / ssumm2 + poly(C2, 6, rsn);
                    fac = Math.sqrt((summ2 - 2.0 * a[1] * a[1] - 2.0 * a[2] * a[2]) / (1.0 - 2.0 * a1 * a1 - 2.0 * a2 * a2));
                    a[1] = a1;
                    a[2] = a2;
                } else {
                    i1 = 2;
                    fac = Math.sqrt((summ2 - 2.0 * a[1] * a[1]) / (1.0 - 2.0 * a1 * a1));
                    a[1] = a1;
                }
                for (int i = i1; i <= nn2; ++i) {
                    a[i] = -a[i] / fac;
                }
            }
            init[0] = true;
        }
        if (n1 < 3) {
            return;
        }
        final int ncens = n - n1;
        ifault[0] = 4;
        if (ncens < 0 || (ncens > 0 && n < 20)) {
            return;
        }
        ifault[0] = 5;
        final double delta = ncens / an;
        if (delta > 0.8) {
            return;
        }

        // If W input as negative, calculate significance level of -W

        double w1, xx;
        if (w[0] < 0.0) {
            w1 = 1.0 + w[0];
            ifault[0] = 0;
        } else {

            // Check for zero range

            ifault[0] = 6;
            final double range = x[n1] - x[1];
            if (range < SMALL) {
                return;
            }

            // Check for correct sort order on range - scaled X

            ifault[0] = 7;
            xx = x[1] / range;
            double sx = xx;
            double sa = -a[1];
            int j = n - 1;
            for (int i = 2; i <= n1; ++i) {
                final double xi = x[i] / range;
                // IF (XX-XI .GT. SMALL) PRINT *,' ANYTHING'
                sx += xi;
                if (i != j) {
                    sa += sign(1, i - j) * a[Math.min(i, j)];
                }
                xx = xi;
                --j;
            }
            ifault[0] = 0;
            if (n > 5000) {
                ifault[0] = 2;
            }

            // Calculate W statistic as squared correlation between data and coefficients

            sa /= n1;
            sx /= n1;
            double ssa = 0.0;
            double ssx = 0.0;
            double sax = 0.0;
            j = n;
            double asa;
            for (int i = 1; i <= n1; ++i) {
                if (i != j) {
                    asa = sign(1, i - j) * a[Math.min(i, j)] - sa;
                } else {
                    asa = -sa;
                }
                final double xsx = x[i] / range - sx;
                ssa += asa * asa;
                ssx += xsx * xsx;
                sax += asa * xsx;
                --j;
            }

            // W1 equals (1-W) calculated to avoid excessive rounding error
            // for W very near 1 (a potential problem in very large samples)

            final double ssassx = Math.sqrt(ssa * ssx);
            w1 = (ssassx - sax) * (ssassx + sax) / (ssa * ssx);
        }
        w[0] = 1.0 - w1;

        // Calculate significance level for W (exact for N=3)

        if (n == 3) {
            pw[0] = PI6 * (Math.asin(Math.sqrt(w[0])) - STQR);
            return;
        }
        double y = Math.log(w1);
        xx = Math.log(an);
        double m = 0.0;
        double s = 1.0;
        if (n <= 11) {
            final double gamma = poly(G, 2, an);
            if (y >= gamma) {
                pw[0] = SMALL;
                return;
            }
            y = -Math.log(gamma - y);
            m = poly(C3, 4, an);
            s = Math.exp(poly(C4, 4, an));
        } else {
            m = poly(C5, 4, xx);
            s = Math.exp(poly(C6, 3, xx));
        }
        if (ncens > 0) {

            // Censoring by proportion NCENS/N. Calculate mean and sd of normal equivalent deviate of W.

            final double ld = -Math.log(delta);
            final double bf = 1.0 + xx * BF1;
            final double z90f = Z90 + bf * Math.pow(poly(C7, 2, Math.pow(XX90, xx)), ld);
            final double z95f = Z95 + bf * Math.pow(poly(C8, 2, Math.pow(XX95, xx)), ld);
            final double z99f = Z99 + bf * Math.pow(poly(C9, 2, xx), ld);

            // Regress Z90F,...,Z99F on normal deviates Z90,...,Z99 to get
            // pseudo-mean and pseudo-sd of z as the slope and intercept

            final double zfm = (z90f + z95f + z99f) / 3.0;
            final double zsd = (Z90 * (z90f - zfm) + Z95 * (z95f - zfm) + Z99 * (z99f - zfm)) / ZSS;
            final double zbar = zfm - zsd * ZM;
            m += zbar * s;
            s *= zsd;
        }
        pw[0] = alnorm((y - m) / s, UPPER);
    }


    /**
     * Constructs an int with the absolute value of x and the sign of y
     *
     * @param x int to copy absolute value from
     * @param y int to copy sign from
     * @return int with absolute value of x and sign of y
     */
    private static int sign(final int x,
                            final int y) {
        int result = Math.abs(x);
        if (y < 0.0) {
            result = -result;
        }
        return result;
    }

    // Constants & polynomial coefficients for ppnd(), slightly renamed to avoid conflicts. Could define
    // them inside ppnd(), but static constants are more efficient.

    // Coefficients for P close to 0.5
    private static final double A0_p = 3.3871327179E+00, A1_p = 5.0434271938E+01, A2_p = 1.5929113202E+02,
            A3_p = 5.9109374720E+01, B1_p = 1.7895169469E+01, B2_p = 7.8757757664E+01, B3_p = 6.7187563600E+01;

    // Coefficients for P not close to 0, 0.5 or 1 (names changed to avoid conflict with swilk())
    private static final double C0_p = 1.4234372777E+00, C1_p = 2.7568153900E+00, C2_p = 1.3067284816E+00,
            C3_p = 1.7023821103E-01, D1_p = 7.3700164250E-01, D2_p = 1.2021132975E-01;

    // Coefficients for P near 0 or 1.
    private static final double E0_p = 6.6579051150E+00, E1_p = 3.0812263860E+00, E2_p = 4.2868294337E-01,
            E3_p = 1.7337203997E-02, F1_p = 2.4197894225E-01, F2_p = 1.2258202635E-02;

    private static final double SPLIT1 = 0.425, SPLIT2 = 5.0, CONST1 = 0.180625, CONST2 = 1.6;


    /**
     * ALGORITHM AS 241 APPL. STATIST. (1988) VOL. 37, NO. 3, 477-484.
     * <p/>
     * Produces the normal deviate Z corresponding to a given lower tail area of P; Z is accurate to about 1 part in 10**7.
     *
     * @param p
     * @return
     */
    private static double ppnd(final double p) {
        final double q = p - 0.5;
        double r;
        if (Math.abs(q) <= SPLIT1) {
            r = CONST1 - q * q;
            return q * (((A3_p * r + A2_p) * r + A1_p) * r + A0_p) / (((B3_p * r + B2_p) * r + B1_p) * r + 1.0);
        } else {
            if (q < 0.0) {
                r = p;
            } else {
                r = 1.0 - p;
            }
            if (r <= 0.0) {
                return 0.0;
            }
            r = Math.sqrt(-Math.log(r));
            double normal_dev;
            if (r <= SPLIT2) {
                r -= CONST2;
                normal_dev = (((C3_p * r + C2_p) * r + C1_p) * r + C0_p) / ((D2_p * r + D1_p) * r + 1.0);
            } else {
                r -= SPLIT2;
                normal_dev = (((E3_p * r + E2_p) * r + E1_p) * r + E0_p) / ((F2_p * r + F1_p) * r + 1.0);
            }
            if (q < 0.0) {
                normal_dev = -normal_dev;
            }
            return normal_dev;
        }
    }


    /**
     * Algorithm AS 181.2 Appl. Statist. (1982) Vol. 31, No. 2
     * <p/>
     * Calculates the algebraic polynomial of order nord-1 with array of coefficients c. Zero order coefficient is c[1]
     *
     * @param c
     * @param nord
     * @param x
     * @return
     */
    private static double poly(final double[] c,
                               final int nord,
                               final double x) {
        double poly = c[1];
        if (nord == 1) {
            return poly;
        }
        double p = x * c[nord];
        if (nord != 2) {
            final int n2 = nord - 2;
            int j = n2 + 1;
            for (int i = 1; i <= n2; ++i) {
                p = (p + c[j]) * x;
                --j;
            }
        }
        poly += p;
        return poly;
    }

    // Constants & polynomial coefficients for alnorm(), slightly renamed to avoid conflicts.
    private static final double CON_a = 1.28, LTONE_a = 7.0, UTZERO_a = 18.66;
    private static final double P_a = 0.398942280444, Q_a = 0.39990348504, R_a = 0.398942280385, A1_a = 5.75885480458,
            A2_a = 2.62433121679, A3_a = 5.92885724438, B1_a = -29.8213557807, B2_a = 48.6959930692, C1_a = -3.8052E-8,
            C2_a = 3.98064794E-4, C3_a = -0.151679116635, C4_a = 4.8385912808, C5_a = 0.742380924027, C6_a = 3.99019417011,
            D1_a = 1.00000615302, D2_a = 1.98615381364, D3_a = 5.29330324926, D4_a = -15.1508972451, D5_a = 30.789933034;


    /**
     * Algorithm AS66 Applied Statistics (1973) vol.22, no.3
     * <p/>
     * Evaluates the tail area of the standardised normal curve from x to infinity if upper is true or from minus infinity to x if
     * upper is false.
     *
     * @param x
     * @param upper
     * @return
     */
    private static double alnorm(final double x,
                                 final boolean upper) {
        boolean up = upper;
        double z = x;
        if (z < 0.0) {
            up = !up;
            z = -z;
        }
        double fn_val;
        if (z > LTONE_a && (!up || z > UTZERO_a)) {
            fn_val = 0.0;
        } else {
            double y = 0.5 * z * z;
            if (z <= CON_a) {
                fn_val = 0.5 - z * (P_a - Q_a * y / (y + A1_a + B1_a / (y + A2_a + B2_a / (y + A3_a))));
            } else {
                fn_val = R_a
                        * Math.exp(-y)
                        / (z + C1_a + D1_a
                        / (z + C2_a + D2_a / (z + C3_a + D3_a / (z + C4_a + D4_a / (z + C5_a + D5_a / (z + C6_a))))));
            }
        }
        if (!up) {
            fn_val = 1.0 - fn_val;
        }
        return fn_val;
    }
}