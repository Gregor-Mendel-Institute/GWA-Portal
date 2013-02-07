package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;


public class HighlightCell extends AbstractCell<String> {

    private static final String replaceString = "<span style='color:red;font-weight:bold;'>$1</span>";
	private SearchTerm searchTerm;
	
	public HighlightCell(SearchTerm searchTerm) {
		super();
		this.searchTerm = searchTerm;
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context ctx,
			String value, SafeHtmlBuilder sb) {
		if (value != null) {
		    if (searchTerm != null) {
		      RegExp searchRegExp = searchTerm.getSearchRegExp();
		      // The search regex has already been html-escaped
		      if (searchRegExp != null) {
		    	  value = searchRegExp.replace(SafeHtmlUtils.htmlEscape(value),replaceString);
		      	  sb.append(SafeHtmlUtils.fromTrustedString(value));
		      }
		      else { 
		    	  sb.appendEscaped(value);
		      }
		    } else {
		    	sb.appendEscaped(value);
		    }
		  }
	}
	
	

}
