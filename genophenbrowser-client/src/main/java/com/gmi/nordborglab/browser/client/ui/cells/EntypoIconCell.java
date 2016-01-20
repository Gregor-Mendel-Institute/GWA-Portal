package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Created by uemit.seren on 1/18/16.
 */
public class EntypoIconCell extends AbstractSafeHtmlCell<String> {


    public EntypoIconCell() {
        super(EntypoIconRenderer.getInstance(), "Click");
    }

    @Override
    protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        sb.append(data);
    }

    @Override
    public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);    //To change body of overridden methods use File | Settings | File Templates.
        if ("click".equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
                if (valueUpdater != null) {
                    valueUpdater.update(value);
                }
            }
        }
    }
}
