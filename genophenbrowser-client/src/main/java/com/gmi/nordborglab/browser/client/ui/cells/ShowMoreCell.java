package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.gwtbootstrap3.client.ui.constants.IconType;


/**
 * Created by uemit.seren on 1/19/16.
 */
public class ShowMoreCell extends AbstractCell<Boolean> {

    private final FontAwesomeIconRenderer renderer = FontAwesomeIconRenderer.getInstance();

    public ShowMoreCell() {
        super("click");
    }

    @Override
    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        sb.append(renderer.render((value ? IconType.MINUS_SQUARE_O : IconType.PLUS_SQUARE_O)));
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, Boolean value, NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
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
