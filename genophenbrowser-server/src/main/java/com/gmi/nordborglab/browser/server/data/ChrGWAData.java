package com.gmi.nordborglab.browser.server.data;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gmi.nordborglab.browser.server.converters.ChrGWADataSerializer;
import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
import com.google.common.primitives.Floats;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@JsonSerialize(using = ChrGWADataSerializer.class)
public class ChrGWAData {

    private final int[] positions;
    private final float[] pvalues;
    private final int[] macs;
    private final float[] mafs;
    private final float[] GVEs;
    private List<SNPInfo> snpInfos;

    private final String chr;

    public ChrGWAData(final int[] positions, final float[] pvalues, final int[] macs, final float[] mafs, final float[] GVEs, final String chr) {
        this.positions = positions;
        this.pvalues = pvalues;
        this.macs = macs;
        this.mafs = mafs;
        this.GVEs = GVEs;
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

    public int[] getMacs() {
        return macs;
    }

    public float[] getMafs() {
        return mafs;
    }

    public float[] getGVEs() {
        return GVEs;
    }

    public static ChrGWAData sortAndConvertToScores(final ChrGWAData chrGWAData) {
        final boolean isPvalues = (Floats.max(chrGWAData.pvalues) <= 1.0);
        Integer[] idx = new Integer[chrGWAData.positions.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                if (isPvalues)
                    return Float.compare(chrGWAData.pvalues[i1], chrGWAData.pvalues[i2]);
                else
                    return Float.compare(chrGWAData.pvalues[i2], chrGWAData.pvalues[i1]);
            }
        });
        int[] newPositions = new int[chrGWAData.positions.length];
        float[] newPvalues = new float[chrGWAData.positions.length];
        int[] newMACs = null;
        float[] newMAFs = null;
        float[] newGVEs = null;
        if (chrGWAData.macs != null && chrGWAData.macs.length > 0)
            newMACs = new int[chrGWAData.positions.length];
        if (chrGWAData.mafs != null && chrGWAData.mafs.length > 0)
            newMAFs = new float[chrGWAData.positions.length];
        if (chrGWAData.GVEs != null && chrGWAData.GVEs.length > 0)
            newGVEs = new float[chrGWAData.positions.length];

        for (int i = 0; i < chrGWAData.positions.length; i++) {
            newPositions[i] = chrGWAData.positions[idx[i]];
            if (newMACs != null)
                newMACs[i] = chrGWAData.macs[idx[i]];
            if (newMAFs != null)
                newMAFs[i] = chrGWAData.mafs[idx[i]];
            if (newGVEs != null)
                newGVEs[i] = chrGWAData.GVEs[idx[i]];
            float pvalue = chrGWAData.pvalues[idx[i]];
            if (isPvalues && pvalue > 0.0) {
                pvalue = (float) -Math.log10((double) pvalue);
            }
            newPvalues[i] = pvalue;
        }
        return new ChrGWAData(newPositions, newPvalues, newMACs, newMAFs, newGVEs, chrGWAData.getChr());
    }

    public static ChrGWAData sortByIndex(final ChrGWAData chrGWAData, Integer[] idx) {
        int[] newPositions = new int[chrGWAData.positions.length];
        float[] newPvalues = new float[chrGWAData.positions.length];
        int[] newMACs = null;
        float[] newMAFs = null;
        float[] newGVEs = null;
        if (chrGWAData.macs != null && chrGWAData.macs.length > 0)
            newMACs = new int[chrGWAData.positions.length];
        if (chrGWAData.mafs != null && chrGWAData.mafs.length > 0)
            newMAFs = new float[chrGWAData.positions.length];
        if (chrGWAData.GVEs != null && chrGWAData.GVEs.length > 0)
            newGVEs = new float[chrGWAData.positions.length];
        for (int i = 0; i < chrGWAData.positions.length; i++) {
            newPositions[i] = chrGWAData.positions[idx[i]];
            if (newMACs != null)
                newMACs[i] = chrGWAData.macs[idx[i]];
            if (newMAFs != null)
                newMAFs[i] = chrGWAData.mafs[idx[i]];
            if (newGVEs != null)
                newGVEs[i] = chrGWAData.GVEs[idx[i]];
            newPvalues[i] = chrGWAData.pvalues[idx[i]];
        }
        return new ChrGWAData(newPositions, newPvalues, newMACs, newMAFs, newGVEs, chrGWAData.getChr());
    }


    public static ChrGWAData sortByPosition(final ChrGWAData chrGWAData) {
        Integer[] idx = new Integer[chrGWAData.positions.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                return Float.compare(chrGWAData.positions[i1], chrGWAData.positions[i2]);
            }
        });
        return sortByIndex(chrGWAData, idx);
    }

    public List<SNPInfo> getSNPInfos() {
        return snpInfos;
    }

    public void setSNPInfos(List<SNPInfo> snpInfos) {
        this.snpInfos = snpInfos;
    }
}
