package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 08.10.13
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class TextBoxFilterItemPresenterWidgetView extends FilterItemPresenterWidgetView<TextBoxFilterItemPresenterWidget.MyView> implements TextBoxFilterItemPresenterWidget.MyView {

    @UiField
    DivElement filterName;
    @UiField
    TextBox filterValue;

    @Override
    public void setFilterName(String name) {
        filterName.setInnerText(name);
    }


    @Override
    public HasText getFlterTB() {
        return filterValue;
    }


    interface Binder extends UiBinder<Widget, TextBoxFilterItemPresenterWidgetView> {

    }

    @Inject
    public TextBoxFilterItemPresenterWidgetView(Binder binder) {
        widget = binder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


}