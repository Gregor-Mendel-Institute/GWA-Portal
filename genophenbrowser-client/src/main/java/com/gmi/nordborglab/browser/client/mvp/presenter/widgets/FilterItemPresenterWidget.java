package com.gmi.nordborglab.browser.client.mvp.presenter.widgets;

import com.gmi.nordborglab.browser.client.events.FilterAddedEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.FilterItemPresenterUiHandlers;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.gmi.nordborglab.browser.shared.proxy.FilterItemValueProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.10.13
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class FilterItemPresenterWidget<C extends FilterItemPresenterWidget.MyView> extends PresenterWidget<C> implements FilterItemPresenterUiHandlers {

    protected ConstEnums.FILTERS filterType;

    protected FilterItem filterItem;
    protected boolean hasMultiple;

    public interface MyView extends View, HasUiHandlers<FilterItemPresenterUiHandlers> {

        void showPopup(boolean show);

        void setFilterLabel(String name);


    }

    protected FilterItemPresenterWidget(EventBus eventBus, C view) {
        super(eventBus, view);
        getView().setUiHandlers(this);
    }

    public void setFilterType(ConstEnums.FILTERS filterType) {
        this.filterType = filterType;
        getView().setFilterLabel(filterType.name());
        init();
    }

    @Override
    public void onOpenFilterSettings() {
        getView().showPopup(true);
    }

    @Override
    public void onCancel() {
        reset();
        getView().showPopup(false);
    }

    public void removeFilterItem(FilterItemValueProxy filterItemValue) {
        filterItem.getValues().remove(filterItemValue);
    }

    @Override
    public void onAdd() {
        List<FilterItemValue> newFiltervalues = createFilterItemValue();
        if (newFiltervalues == null)
            return;
        if (filterItem == null) {
            filterItem = new FilterItem(filterType, newFiltervalues);
        } else {
            if (hasMultiple) {
                filterItem.getValues().addAll(newFiltervalues);
            } else {
                filterItem.setValues(newFiltervalues);
            }
        }
        reset();
        fireEvent();
        getView().showPopup(false);
    }

    public void resetActiveFilterItem(boolean fireEvent) {
        this.filterItem = null;
        if (fireEvent) {
            fireEvent();
        }
    }

    public FilterItem getActiveFilterItem() {
        return filterItem;
    }

    @Override
    public void onSearchByQuery(SuggestOracle.Request request, SuggestOracle.Callback callback) {

    }

    abstract List<FilterItemValue> createFilterItemValue();


    protected abstract void init();

    protected abstract void reset();

    public void setHasMultiple(boolean hasMultiple) {
        this.hasMultiple = hasMultiple;
    }

    public void setFilterItemValue(List<FilterItemValue> values) {
        setFilterItemValue(values, true, true);
    }

    public void setFilterItemValue(List<FilterItemValue> values, boolean isAdd, boolean fireEvent) {
        if (filterItem == null) {
            filterItem = new FilterItem(filterType, values);
        } else {
            if (isAdd) {
                filterItem.getValues().addAll(values);
            } else {
                filterItem.setValues(values);
            }
        }
        if (fireEvent) {
            fireEvent();
        }
    }

    protected void fireEvent() {
        getEventBus().fireEventFromSource(new FilterAddedEvent(), this);
    }

}
