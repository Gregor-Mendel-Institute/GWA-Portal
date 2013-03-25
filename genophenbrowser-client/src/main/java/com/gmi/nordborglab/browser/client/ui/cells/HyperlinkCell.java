package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class HyperlinkCell extends AbstractCell<String[]> {

	interface Template extends SafeHtmlTemplates {
		@Template("<a href=\"{0}\" target=\"{2}\">{1}</a>")
		SafeHtml hyperText(SafeUri link, String text,String target);
	}

	private static Template template = GWT.create(Template.class);;
    private final boolean isNewWindow;

	public static final int LINK_INDEX = 0, URL_INDEX = 1;

	/**
	 * Construct a new ImageCell.
	 */

    public HyperlinkCell(boolean isNewWindow) {
       this.isNewWindow = isNewWindow;
    }
	public HyperlinkCell() {
        this(false);
	}

	@Override
	public void render(Context context, String[] value, SafeHtmlBuilder sb) {
		if (value != null) {
            String target = "";
            if (isNewWindow)
                target = "_blank";
			// The template will sanitize the URI.
			sb.append(template.hyperText(
					UriUtils.fromString(value[LINK_INDEX]), value[URL_INDEX],target));
		}
	}
}