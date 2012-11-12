package com.gmi.nordborglab.browser.client.util;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.visualization.client.DataTable;



public class CustomDataTable extends DataTable {
	
	protected CustomDataTable() {}
	
	public static class Filter extends JavaScriptObject {
	
		protected Filter() {}
		
		public final native void setColumn(int column) /*-{
			this.column = column;
		}-*/;
		
		public final native void setValue(int value) /*-{
			this.value = value;
		}-*/;
		
		public final native void setMinValue(int minValue) /*-{
			this.minValue = minValue;
		}-*/;
		
		public final native void setMaxValue(int maxValue) /*-{
			this.maxValue = maxValue;
		}-*/;
	}

	public final native JsArrayInteger getFilteredRows(JsArray<Filter> filters) /*-{
    	return this.getFilteredRows(filters);
  	}-*/;
	
	
}
