package com.gmi.nordborglab.browser.server.domain;

public class BreadcrumbItem {

	private Long id;
	private String text;
	private String type;
	
	public BreadcrumbItem() {
		
	}
	
	public BreadcrumbItem(Long id,String text,String type) {
		this.id = id;
		this.text = text;
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
