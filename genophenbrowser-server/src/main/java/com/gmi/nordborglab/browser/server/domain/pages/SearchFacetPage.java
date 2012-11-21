package com.gmi.nordborglab.browser.server.domain.pages;

import java.awt.print.Pageable;
import java.util.List;

import com.gmi.nordborglab.browser.server.domain.SearchItem;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;

public class SearchFacetPage {

	private List<SearchItem> contents;
	private long total;
	private SUB_CATEGORY category; 
	
	
	
	public SearchFacetPage(List<SearchItem> contents,Pageable pageable,long total,SUB_CATEGORY category) {
		this.contents = contents;
		this.total = total;
		this.category = category;
	}
	
	
	public List<SearchItem> getContents() {
		return contents;
	}
	
	public long getTotal() {
		return total;
	}
	
	public SUB_CATEGORY getCategory() {
		return category;
	}
}
