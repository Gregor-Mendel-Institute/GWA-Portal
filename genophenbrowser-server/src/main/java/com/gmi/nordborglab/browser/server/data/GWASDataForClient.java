package com.gmi.nordborglab.browser.server.data;

import java.util.List;

public class GWASDataForClient {

	protected final double maxScore;
	protected final double bonferroniThreshold;
	protected final List<String> chromosomes;
	protected final List<Integer> chrLengths;
	protected final List<String> gwasData;
	
	public GWASDataForClient(double maxScore,double bonferroniThreshold,List<String> chromosomes,List<Integer> chrLengths,List<String> gwasData) {
		this.maxScore = maxScore;
		this.bonferroniThreshold = bonferroniThreshold;
		this.chrLengths = chrLengths;
		this.chromosomes = chromosomes;
		this.gwasData = gwasData;
	}

	public double getMaxScore() {
		return maxScore;
	}

	public double getBonferroniThreshold() {
		return bonferroniThreshold;
	}

	public List<String> getChromosomes() {
		return chromosomes;
	}

	public List<Integer> getChrLengths() {
		return chrLengths;
	}

	public List<String> getGwasData() {
		return gwasData;
	}
	
}
