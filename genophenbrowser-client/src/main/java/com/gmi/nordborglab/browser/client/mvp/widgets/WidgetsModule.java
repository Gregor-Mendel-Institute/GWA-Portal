package com.gmi.nordborglab.browser.client.mvp.widgets;

import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.DropDownFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.DropDownFilterItemPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.FilterPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.FilterPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.TextBoxFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.TextBoxFilterItemPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.TypeaheadFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.filter.TypeaheadFilterItemPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.widgets.gwas.GWASPlotPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.gwas.GWASPlotView;
import com.gmi.nordborglab.browser.client.mvp.widgets.gwas.GWASUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.gwas.GWASUploadWizardView;
import com.gmi.nordborglab.browser.client.mvp.widgets.permissions.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.permissions.PermissionDetailView;
import com.gmi.nordborglab.browser.client.mvp.widgets.search.SearchPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.search.SearchView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class WidgetsModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenterWidget(GWASPlotPresenterWidget.class,
                GWASPlotPresenterWidget.MyView.class,
                GWASPlotView.class);

        bindPresenterWidget(FilterPresenterWidget.class,
                FilterPresenterWidget.MyView.class,
                FilterPresenterWidgetView.class);

        bindPresenterWidget(TextBoxFilterItemPresenterWidget.class,
                TextBoxFilterItemPresenterWidget.MyView.class,
                TextBoxFilterItemPresenterWidgetView.class);

        bindPresenterWidget(DropDownFilterItemPresenterWidget.class,
                DropDownFilterItemPresenterWidget.MyView.class,
                DropDownFilterItemPresenterWidgetView.class);

        bindPresenterWidget(TypeaheadFilterItemPresenterWidget.class,
                TypeaheadFilterItemPresenterWidget.MyView.class,
                TypeaheadFilterItemPresenterWidgetView.class);

        bindPresenterWidget(FacetSearchPresenterWidget.class,
                FacetSearchPresenterWidget.MyView.class,
                FacetSearchPresenterWidgetView.class);

        bindSingletonPresenterWidget(GWASUploadWizardPresenterWidget.class, GWASUploadWizardPresenterWidget.MyView.class, GWASUploadWizardView.class);

        bindPresenterWidget(PermissionDetailPresenter.class,
                PermissionDetailPresenter.MyView.class,
                PermissionDetailView.class);

        bindPresenterWidget(SearchPresenter.class,
                SearchPresenter.MyView.class, SearchView.class);
    }
}
