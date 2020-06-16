package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;


/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntypoIconActionCell<C> extends AbstractCell<C> {

    private final String entypoString;
    private final ActionCell.Delegate<C> delegate;
    private final boolean hasMargin;

    interface Template extends SafeHtmlTemplates {

        @SafeHtmlTemplates.Template("<span style=\"{0}\"><i class=\"{1}\" ></i></span>")
        SafeHtml cell(SafeStyles styles, String iconClass);

    }

    private static Template templates = GWT.create(Template.class);

    public EntypoIconActionCell(final String entypoString) {
        this(entypoString, null, false);
    }

    public EntypoIconActionCell(String entypoString, ActionCell.Delegate<C> delegate, boolean hasMargin) {
        super("click");
        this.entypoString = entypoString;
        this.delegate = delegate;
        this.hasMargin = hasMargin;
    }

    public EntypoIconActionCell(final String entypoString, ActionCell.Delegate<C> delegate) {
        this(entypoString, delegate, false);
    }


    @Override
    public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, C value, NativeEvent event, ValueUpdater<C> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);    //To change body of overridden methods use File | Settings | File Templates.
        if ("click".equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
                if (delegate != null) {
                    delegate.execute(value);
                }
                if (valueUpdater != null) {
                    valueUpdater.update(value);
                }
            }
        }
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb) {
        SafeStylesBuilder builder = new SafeStylesBuilder();
        builder.append(SafeStylesUtils.forFontSize(19, Style.Unit.PX));
        if (hasMargin) {
            builder.append(SafeStylesUtils.forMarginRight(5, Style.Unit.PX));
        }
        builder.append(SafeStylesUtils.forCursor(Style.Cursor.POINTER));
        SafeHtml rendered = templates.cell(builder.toSafeStyles(), entypoString);
        sb.append(rendered);
    }
}
