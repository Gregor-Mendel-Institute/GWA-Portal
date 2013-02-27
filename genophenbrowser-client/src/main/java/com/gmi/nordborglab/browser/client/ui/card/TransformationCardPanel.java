package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.client.ui.card.TransformationCard;
import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/22/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformationCardPanel extends ResizeComposite implements HasWidgets {


    protected SimpleLayoutPanel panel;

    public TransformationCardPanel() {
        panel  = new SimpleLayoutPanel();
        initWidget(panel);
    }

    @Override
    public void setWidget(Widget w) {
        super.setWidget(w);
    }

    @Override
    public void onResize() {
       ((TransformationCard)panel.getWidget()).onResize();
    }

    @Override
    public Widget getWidget()  {
        return panel;
    }

    @Override
    public void add(Widget w) {
        panel.add(w);
    }

    @Override
    public void clear() {
        panel.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return panel.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return panel.remove(w);
    }
}
