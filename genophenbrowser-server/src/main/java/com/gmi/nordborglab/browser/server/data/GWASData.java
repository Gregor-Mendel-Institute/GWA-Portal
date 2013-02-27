package com.gmi.nordborglab.browser.server.data;


import com.google.common.primitives.Floats;

import java.util.Arrays;
import java.util.Comparator;

public class GWASData {
	
	private final int[] positions;
	private final float[] pvalues;
	private final String chr;
	
	public GWASData(final int[] positions, final float[] pvalues,final String chr) {
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

    public static GWASData sortAndConvertToScores(final GWASData gwasData) {
        final boolean isPvalues = (Floats.max(gwasData.pvalues) <= 1.0);
        Integer[] idx = new Integer[gwasData.positions.length];
        for( int i = 0 ; i < idx.length; i++ ) idx[i] = i;
        Arrays.sort(idx,new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                if (isPvalues)
                    return Float.compare(gwasData.pvalues[i1], gwasData.pvalues[i2]);
                else
                    return Float.compare(gwasData.pvalues[i2], gwasData.pvalues[i1]);
            }
        });
        int[] newPositions = new int[gwasData.positions.length];
        float[] newPvalues = new float[gwasData.positions.length];
        for (int i =0;i< gwasData.positions.length;i++)   {
            newPositions[i] = gwasData.positions[idx[i]];
            float pvalue = gwasData.pvalues[idx[i]];
            if (isPvalues && pvalue > 0.0) {
                pvalue = (float)-Math.log10((double)pvalue);
            }
            newPvalues[i] = pvalue;
        }
        return new GWASData(newPositions,newPvalues,gwasData.getChr());
    }

}
