package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.gmi.nordborglab.browser.client.events.FilterAddedEvent;
import com.gmi.nordborglab.browser.client.events.FilterModifiedEvent;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.10.13
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class FilterPresenterWidget extends PresenterWidget<FilterPresenterWidget.MyView> implements FilterPresenterUiHandlers {


    public interface MyView extends View, HasUiHandlers<FilterPresenterUiHandlers> {

        void resetActiveFilters();

        void addFilterItem(FilterItem filterItem);

        void resetAvailableFilters();

    }

    public static final Object TYPE_FilterItemsContent = new Object();

    private List<FilterItemPresenterWidget> filterItemWidgets;
    private Boolean hasMultiple = null;

    @Inject
    public FilterPresenterWidget(EventBus eventBus, MyView view) {
        super(eventBus, view);
        getView().setUiHandlers(this);
        //  this.localEventBus = new SimpleEventBus();
    }

    public void setFilterItemWidgets(List<FilterItemPresenterWidget> filterItemWidgets) {
        this.filterItemWidgets = filterItemWidgets;
        getView().resetActiveFilters();
        getView().resetAvailableFilters();
        for (FilterItemPresenterWidget filterItem : filterItemWidgets) {
            if (hasMultiple != null) {
                filterItem.setHasMultiple(hasMultiple);
            }
            addToSlot(TYPE_FilterItemsContent, filterItem);
        }
    }


    @Override
    protected void onBind() {
        super.onBind();    //To change body of overridden methods use File | Settings | File Templates.
        registerHandler(getEventBus().addHandler(FilterAddedEvent.TYPE, new FilterAddedEvent.Handler() {
            @Override
            public void onFilterAdded(FilterAddedEvent event) {
                // check event source and handle event
                if (filterItemWidgets.contains(event.getSource())) {
                    updateActiveFilters();
                }
                getEventBus().fireEventFromSource(new FilterModifiedEvent(), FilterPresenterWidget.this);
            }
        }));
    }

    private void updateActiveFilters() {
        getView().resetActiveFilters();
        for (FilterItem filterItem : getActiveFilterItems()) {
            getView().addFilterItem(filterItem);
        }
    }

    @Override
    public void removeFilter(FilterItem filterItem) {
        for (FilterItemPresenterWidget filterItemWidget : filterItemWidgets) {
            if (filterItem.equals(filterItemWidget.getActiveFilterItem())) {
                filterItemWidget.resetActiveFilterItem(false);
                break;
            }
        }
        updateActiveFilters();
        getEventBus().fireEventFromSource(new FilterModifiedEvent(), FilterPresenterWidget.this);
    }

    public List<FilterItem> getActiveFilterItems() {
        List<FilterItem> activeFilterItems = Lists.newArrayList();
        for (FilterItemPresenterWidget filterItem : filterItemWidgets) {
            if (filterItem.getActiveFilterItem() != null) {
                activeFilterItems.add(filterItem.getActiveFilterItem());
            }
        }
        return activeFilterItems;
    }

    public void setHasMultiple(Boolean hasMultiple) {
        if (this.hasMultiple != hasMultiple && hasMultiple != null) {
            if (filterItemWidgets != null) {
                for (FilterItemPresenterWidget filterWidget : filterItemWidgets) {
                    filterWidget.setHasMultiple(hasMultiple);
                }
            }
        }
        this.hasMultiple = hasMultiple;
    }

    // private final EventBus localEventBus;
    public void reset(boolean fireEvent) {
        for (FilterItemPresenterWidget filterWidget : filterItemWidgets) {
            filterWidget.resetActiveFilterItem(false);
        }
        getView().resetActiveFilters();
        if (fireEvent) {
            getEventBus().fireEventFromSource(new FilterModifiedEvent(), FilterPresenterWidget.this);
        }
    }

}
