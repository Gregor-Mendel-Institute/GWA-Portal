package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 13:01
 * To change this template use File | Settings | File Templates.
 */
public class DropDownFilterItemPresenterWidget extends FilterItemPresenterWidget<DropDownFilterItemPresenterWidget.MyView> {

    public interface MyView extends FilterItemPresenterWidget.MyView {
        List<String[]> getSelectedValues();

        void setAvailableValues(List<String[]> values);

        void setFilterName(String name);

        void clearSelection();

        void setHasMultiple(boolean hasMultiple);
    }


    @Inject
    public DropDownFilterItemPresenterWidget(EventBus eventBus, MyView view) {
        super(eventBus, view);
    }

    @Override
    List<FilterItemValue> createFilterItemValue() {
        List<String[]> selectedValues = getView().getSelectedValues();
        if (selectedValues == null)
            return null;
        return Lists.newArrayList(Lists.transform(selectedValues, new Function<String[], FilterItemValue>() {
            @Nullable
            @Override
            public FilterItemValue apply(String[] strings) {
                Preconditions.checkNotNull(strings);
                return new FilterItemValue(strings[0], strings[1]);
            }
        }));
    }

    @Override
    protected void init() {
        getView().setFilterName(filterType.name());
        getView().setHasMultiple(hasMultiple);
    }

    @Override
    protected void reset() {
        getView().clearSelection();
    }

    public void setAvailableOptions(List<String[]> options) {
        getView().setAvailableValues(options);
    }
}
