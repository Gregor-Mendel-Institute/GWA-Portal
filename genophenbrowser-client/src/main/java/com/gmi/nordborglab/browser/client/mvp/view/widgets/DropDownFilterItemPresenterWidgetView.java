package com.gmi.nordborglab.browser.client.mvp.view.widgets;

import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.DropDownFilterItemPresenterWidget;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.watopi.chosen.client.gwt.ChosenListBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */
public class DropDownFilterItemPresenterWidgetView extends FilterItemPresenterWidgetView<DropDownFilterItemPresenterWidget.MyView> implements DropDownFilterItemPresenterWidget.MyView {

    interface Binder extends UiBinder<Widget, DropDownFilterItemPresenterWidgetView> {

    }

    private final Widget widget;
    private Map<String, String> valueToText = new HashMap<String, String>();

    @UiField
    ChosenListBox filterDD;
    @UiField
    DivElement filterName;

    @Inject
    public DropDownFilterItemPresenterWidgetView(Binder binder) {
        widget = binder.createAndBindUi(this);
        filterDD.setDisableSearchThreshold(10);
        container.setHeight("250px");
        filterDD.setWidth("300px");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setAvailableValues(List<String[]> values) {
        valueToText.clear();
        for (String[] value : values) {
            filterDD.addItem(value[0], value[1]);
            valueToText.put(value[1], value[0]);
        }
        filterDD.update();
    }

    @Override
    public void setFilterName(String name) {
        filterName.setInnerText(name);
        filterDD.setPlaceholderText("Choose your " + name + " filter...");
    }

    @Override
    public void clearSelection() {
        filterDD.setSelectedIndex(-1);
    }


    @Override
    public List<String[]> getSelectedValues() {
        if (filterDD.getSelectedIndex() == -1)
            return null;
        String[] selectedValues = filterDD.getValues();
        if (selectedValues == null)
            return null;
        List<String[]> values = Lists.newArrayList();
        for (String selectedValue : selectedValues) {
            String text = valueToText.get(selectedValue);
            String[] value = {text, selectedValue};
            values.add(value);
        }
        return values;
    }

}