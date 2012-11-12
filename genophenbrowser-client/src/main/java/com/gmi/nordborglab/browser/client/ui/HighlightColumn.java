package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.client.ui.HighlightCell.SearchTerm;
import com.google.gwt.user.cellview.client.Column;

public abstract class HighlightColumn<T> extends Column<T,String>{
	
	
	public HighlightColumn(SearchTerm searchTerm)  {
		super(new HighlightCell(searchTerm));
		
	}
	

}
