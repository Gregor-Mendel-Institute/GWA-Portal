package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class FlagCell extends IconCellDecorator<String> {
	
	private final FlagMap flagMap;
	
	
	public FlagCell(FlagMap flagMap) {
		super(flagMap.getMap().get("UNK"), new TextCell());
		this.flagMap = flagMap;
	}


	@Override
	protected SafeHtml getIconHtml(String value) {
		ImageResource img = flagMap.getMap().get("UNK");
		if (flagMap.getMap().containsKey(value))
			img = flagMap.getMap().get(value);
		AbstractImagePrototype image  = AbstractImagePrototype.create(img);
		SafeHtml imageHTML = image.getSafeHtml();
		return imageHTML;
	}
	

}
