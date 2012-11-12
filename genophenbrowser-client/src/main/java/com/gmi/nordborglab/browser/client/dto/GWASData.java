package com.gmi.nordborglab.browser.client.dto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;

public class GWASData extends JavaScriptObject {
	
	protected GWASData() {}
	
	public final native double getMaxScore() /*-{
		return this.maxScore;
	}-*/;
	
	public final native double getBonferroniThreshold() /*-{
		return this.bonferroniThreshold;
	}-*/;
	
	public final native JsArrayString getChromosomes() /*-{
		return this.chromosomes;
	}-*/;
	
	public final native JsArrayInteger getChrLengths() /*-{
	    return this.chrLengths;
	}-*/;
	
	public final native JsArrayString getGWASDataTablesJSON() /*-{
		return this.gwasData;
	}-*/;
}
