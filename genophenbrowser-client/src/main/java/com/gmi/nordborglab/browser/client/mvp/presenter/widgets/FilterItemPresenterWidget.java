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
            filterItem.getValues().addAll(newFiltervalues);
        }
        reset();
        getEventBus().fireEventFromSource(new FilterAddedEvent(), this);
        getView().showPopup(false);
    }

    public void setActiveFilterItem(FilterItem filterItem) {
        this.filterItem = filterItem;
    }

    public FilterItem getActiveFilterItem() {
        return filterItem;
    }

    @Override
    public void onSearchByQuery(SuggestOracle.Request request, SuggestOracle.Callback callback) {

    }


    abstract List<FilterItemValue> createFilterItemValue();

    abstract void init();

    abstract void reset();

}
