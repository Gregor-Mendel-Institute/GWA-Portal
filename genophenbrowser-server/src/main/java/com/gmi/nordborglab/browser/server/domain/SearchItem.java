package com.gmi.nordborglab.browser.server.domain;

import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;

public class SearchItem {

	private final String id;
	private final String displayText;
	private final String replacementText;
	
	private final CATEGORY category;
	private final SUB_CATEGORY subCategory;
	
	public SearchItem(String id, String displayText,String replacementText,CATEGORY category,SUB_CATEGORY subCategory) {
		this.id = id;
		this.displayText = displayText;
		this.category = category;
		this.replacementText = replacementText;
		this.subCategory = subCategory;
	}

	public String getId() {
		return id;
	}

	public String getDisplayText() {
		return displayText;
	}
	
	public String getReplacementText() {
		return replacementText;
	}

	public CATEGORY getCategory() {
		return category;
	}
	
	public SUB_CATEGORY getSubCategory() {
		return subCategory;
	}
	
}
