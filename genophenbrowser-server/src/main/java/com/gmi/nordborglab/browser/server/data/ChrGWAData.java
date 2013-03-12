package com.gmi.nordborglab.browser.server.data;


import com.google.common.primitives.Floats;

import java.util.Arrays;
import java.util.Comparator;

public class ChrGWAData {
	
	private final int[] positions;
	private final float[] pvalues;
	private final String chr;
	
	public ChrGWAData(final int[] positions, final float[] pvalues, final String chr) {
		this.positions = positions;
		this.pvalues = pvalues;
		this.chr = chr;
	}

	public int[] getPositions() {
		return positions;
	}

	public float[] getPvalues() {
		return pvalues;
	}

	public String getChr() {
		return chr;
	}

    public static ChrGWAData sortAndConvertToScores(final ChrGWAData chrGWAData) {
        final boolean isPvalues = (Floats.max(chrGWAData.pvalues) <= 1.0);
        Integer[] idx = new Integer[chrGWAData.positions.length];
        for( int i = 0 ; i < idx.length; i++ ) idx[i] = i;
        Arrays.sort(idx,new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                if (isPvalues)
                    return Float.compare(chrGWAData.pvalues[i1], chrGWAData.pvalues[i2]);
                else
                    return Float.compare(chrGWAData.pvalues[i2], chrGWAData.pvalues[i1]);
            }
        });
        int[] newPositions = new int[chrGWAData.positions.length];
        float[] newPvalues = new float[chrGWAData.positions.length];
        for (int i =0;i< chrGWAData.positions.length;i++)   {
            newPositions[i] = chrGWAData.positions[idx[i]];
            float pvalue = chrGWAData.pvalues[idx[i]];
            if (isPvalues && pvalue > 0.0) {
                pvalue = (float)-Math.log10((double)pvalue);
            }
            newPvalues[i] = pvalue;
        }
        return new ChrGWAData(newPositions,newPvalues, chrGWAData.getChr());
    }

}
