package com.gmi.nordborglab.browser.client.ui;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class HyperlinkCell extends AbstractCell<String[]> {

	interface Template extends SafeHtmlTemplates {
		@Template("<a href=\"{0}\">{1}</a>")
		SafeHtml hyperText(SafeUri link, String text);
	}

	private static Template template = GWT.create(Template.class);;

	public static final int LINK_INDEX = 0, URL_INDEX = 1;

	/**
	 * Construct a new ImageCell.
	 */
	public HyperlinkCell() {
	}

	@Override
	public void render(Context context, String[] value, SafeHtmlBuilder sb) {
		if (value != null) {
			// The template will sanitize the URI.
			sb.append(template.hyperText(
					UriUtils.fromString(value[LINK_INDEX]), value[URL_INDEX]));
		}
	}
}