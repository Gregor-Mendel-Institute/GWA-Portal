package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.10.13
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
public class TextBoxFilterItemPresenterWidget extends FilterItemPresenterWidget<TextBoxFilterItemPresenterWidget.MyView> {


    public interface MyView extends FilterItemPresenterWidget.MyView {

        void setFilterName(String name);

        HasText getFlterTB();
    }


    @Inject
    protected TextBoxFilterItemPresenterWidget(EventBus eventBus, MyView view) {
        super(eventBus, view);
    }

    @Override
    List<FilterItemValue> createFilterItemValue() {
        String name = getView().getFlterTB().getText();
        if (name == null || name.equals("")) {
            return null;
        }
        return Lists.newArrayList(new FilterItemValue(name, null));
    }

    @Override
    protected void init() {
        getView().setFilterName(filterType.name());
    }

    @Override
    protected void reset() {
        getView().getFlterTB().setText("");
    }


}
