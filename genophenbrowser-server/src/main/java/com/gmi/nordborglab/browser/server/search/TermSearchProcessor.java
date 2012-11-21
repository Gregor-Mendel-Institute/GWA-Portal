package com.gmi.nordborglab.browser.server.search;


public abstract class TermSearchProcessor implements SearchProcessor {
	
	protected String term;
	
	public TermSearchProcessor(String term) {
		this.term = term;
	}
	
	public Long getLongValue() {
		Long value = null;
		try {
			value =  Long.parseLong(term);
		}
		catch (Exception ex) {
			
		}
		return value;
	}

	
}
