package com.gmi.nordborglab.browser.server.data;


public class GWASData {
	
	private final int[] positions;
	private final float[] pvalues;
	private final String chr;
	
	public GWASData(final int[] positions,final float[] pvalues,final String chr) {
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

}
