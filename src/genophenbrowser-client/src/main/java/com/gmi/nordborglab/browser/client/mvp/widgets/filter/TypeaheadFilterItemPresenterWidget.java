package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.gmi.nordborglab.browser.client.manager.SearchManager;
import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 13:01
 * To change this template use File | Settings | File Templates.
 */
public class TypeaheadFilterItemPresenterWidget extends FilterItemPresenterWidget<TypeaheadFilterItemPresenterWidget.MyView> {

    public interface MyView extends FilterItemPresenterWidget.MyView {

        void setFilterName(String name);

        HasText getSearchTb();

        String[] getSearchSelectedValue();
    }

    private final SearchManager searchManager;


    @Inject
    public TypeaheadFilterItemPresenterWidget(EventBus eventBus, MyView view,
                                              SearchManager searchManager) {
        super(eventBus, view);
        this.getView().setUiHandlers(this);
        this.searchManager = searchManager;
    }

    @Override
    List<FilterItemValue> createFilterItemValue() {
        String[] value = getView().getSearchSelectedValue();
        if (value == null)
            return null;
        return Lists.newArrayList(new FilterItemValue(value[0], value[1]));
    }

    @Override
    protected void init() {
        getView().setFilterName(filterType.name());
    }

    @Override
    protected void reset() {
        getView().getSearchTb().setText("");
    }

    @Override
    public void onSearchByQuery(final String request, final SearchManager.SearchCallback callback) {
        searchManager.searchByFilter(request, filterType, callback);
    }
}