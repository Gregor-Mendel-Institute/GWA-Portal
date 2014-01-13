package com.gmi.nordborglab.browser.client.mvp.view.widgets;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.client.mvp.handlers.FilterPresenterUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FilterPresenterWidget;
import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import javax.annotation.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.10.13
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
public class FilterPresenterWidgetView extends ViewWithUiHandlers<FilterPresenterUiHandlers> implements FilterPresenterWidget.MyView {

    interface Binder extends UiBinder<Widget, FilterPresenterWidgetView> {

    }

    public interface MyStyle extends CssResource {
        String active_item();

        String close_icon();
    }

    private final Widget widget;
    @UiField
    UListElement activeFilterContainer;
    @UiField
    HTMLPanel availableFilterContainer;
    @UiField
    UListElement availableFilterContainerUl;

    @UiField
    MyStyle style;

    private BiMap<Icon, FilterItem> filterItemToIcon;
    private ClickHandler removeHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            FilterItem filterItem = filterItemToIcon.get(event.getSource());
            getUiHandlers().removeFilter(filterItem);
        }
    };

    @Inject
    public FilterPresenterWidgetView(Binder binder) {
        widget = binder.createAndBindUi(this);
        filterItemToIcon = HashBiMap.create();
    }


    @Override
    public Widget asWidget() {
        return widget;
    }

    private void clearAvailableFilters() {
        availableFilterContainerUl.setInnerHTML("");
    }

    private void addFilter(IsWidget content) {
        LIElement liItem = Document.get().createLIElement();
        availableFilterContainerUl.appendChild(liItem);
        availableFilterContainer.add(content.asWidget(), liItem);
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {
        if (slot == FilterPresenterWidget.TYPE_FilterItemsContent) {
            addFilter(content);
        } else {
            super.addToSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }


    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == FilterPresenterWidget.TYPE_FilterItemsContent) {
            clearAvailableFilters();
            addToSlot(slot, content);
        } else {
            super.setInSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    @Override
    public void resetActiveFilters() {
        activeFilterContainer.setInnerHTML("");
        filterItemToIcon.clear();
    }

    @Override
    public void addFilterItem(FilterItem filterItem) {
        LIElement liItem = Document.get().createLIElement();
        DivElement divItem = Document.get().createDivElement();
        divItem.setClassName(style.active_item());
        SpanElement spanItem = Document.get().createSpanElement();
        spanItem.setInnerText(getFilterText(filterItem));
        divItem.appendChild(spanItem);
        liItem.appendChild(divItem);
        activeFilterContainer.appendChild(liItem);
        Icon icon = new Icon(IconType.REMOVE);
        icon.addStyleName(style.close_icon());
        availableFilterContainer.add(icon, divItem);
        icon.addDomHandler(removeHandler, ClickEvent.getType());
        filterItemToIcon.put(icon, filterItem);
    }

    @Override
    public void resetAvailableFilters() {
        clearAvailableFilters();
    }

    private String getFilterText(FilterItem filterItem) {
        return filterItem.getType().name() + " = " + Joiner.on(", ").skipNulls().join(Lists.transform(filterItem.getValues(), new Function<FilterItemValue, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable FilterItemValue filterItemValue) {
                if (filterItemValue == null)
                    return null;
                return filterItemValue.getText();
            }
        }));
    }
}