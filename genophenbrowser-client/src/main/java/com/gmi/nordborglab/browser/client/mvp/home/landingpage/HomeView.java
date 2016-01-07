package com.gmi.nordborglab.browser.client.mvp.home.landingpage;

import com.eemi.gwt.tour.client.GwtTour;
import com.eemi.gwt.tour.client.Tour;
import com.gmi.nordborglab.browser.client.bootstrap.BootstrapperImpl;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.ui.cells.NewsItemCell;
import com.gmi.nordborglab.browser.shared.proxy.AppStatProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramFacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;
import com.gmi.nordborglab.browser.shared.proxy.NewsItemProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.LineChart;
import com.googlecode.gwt.charts.client.corechart.LineChartOptions;
import com.googlecode.gwt.charts.client.format.DateFormat;
import com.googlecode.gwt.charts.client.format.DateFormatOptions;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.AnimationEasing;
import com.googlecode.gwt.charts.client.options.CurveType;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.VAxis;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.RadioButton;

import java.util.List;
import java.util.Map;

public class HomeView extends ViewWithUiHandlers<HomeUiHandlers> implements HomePresenter.MyView {

    private final Widget widget;

    @UiField
    SimpleLayoutPanel container;
    @UiField
    Anchor wizardLink;
    @UiField
    SpanElement statsUsers;
    @UiField
    SpanElement statsStudies;
    @UiField
    SpanElement statsPhenotypes;
    @UiField
    SpanElement statsAnalysis;
    @UiField
    SpanElement statsOntologies;
    @UiField
    SpanElement statsPublications;
    @UiField(provided = true)
    CellList<NewsItemProxy> newsList;
    @UiField
    Button tourBtn;
    @UiField
    ImageElement gwappCookieImg;
    @UiField
    LineChart recentDataChart;
    @UiField
    SpanElement chartTypeLb;
    @UiField
    RadioButton studyTypeBtn;
    @UiField
    RadioButton phenotypeBtn;
    @UiField
    RadioButton analysisBtn;

    @UiField
    RadioButton weekBtn;
    @UiField
    RadioButton monthBtn;
    @UiField
    RadioButton yearBtn;
    @UiField
    AnchorButton contactEmailLink;

    private final Tour tour;
    private final PlaceManager placeManager;
    private final Map<AppStatProxy.STAT, SpanElement> statMap;

    public interface Binder extends UiBinder<Widget, HomeView> {

    }

    @Inject
    public HomeView(final Binder binder, final PlaceManager placeManager, final NewsItemCell newsItemCell,
                    @Named("welcome") final Tour tour) {
        this.tour = tour;
        this.placeManager = placeManager;
        newsList = new CellList<NewsItemProxy>(newsItemCell, new EntityProxyKeyProvider<NewsItemProxy>());
        newsList.setEmptyListWidget(new HTML("No news found"));
        widget = binder.createAndBindUi(this);
        tourBtn.getElement().setId("tourBtn");
        statMap = ImmutableMap.<AppStatProxy.STAT, SpanElement>builder()
                .put(AppStatProxy.STAT.USER, statsUsers)
                .put(AppStatProxy.STAT.STUDY, statsStudies)
                .put(AppStatProxy.STAT.PHENOTYPE, statsPhenotypes)
                .put(AppStatProxy.STAT.ANALYSIS, statsAnalysis)
                .put(AppStatProxy.STAT.ONTOLOGY, statsOntologies)
                .put(AppStatProxy.STAT.PUBLICATION, statsPublications).build();


        String gwappCookieUrl = "";
        if (Cookies.getCookieNames().contains("GWAS_USER_ID")) {
            gwappCookieUrl = "http://gwapp.gmi.oeaw.ac.at?datasetkey=" + Cookies.getCookie("GWAS_USER_ID");
        }
        gwappCookieImg.setSrc(gwappCookieUrl);
        contactEmailLink.setHref(UriUtils.fromString("mailto:" + BootstrapperImpl.getContactEmail()).asString());
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == HomePresenter.TYPE_SetMainContent) {
            setMainContent(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    private void setMainContent(IsWidget content) {
        if (content != null) {
            container.setWidget(content);
        }
    }

    @Override
    public void setLinkToWizard(boolean isLoggedIn) {
        String studyWizardLink = placeManager.buildHistoryToken(new PlaceRequest.Builder().nameToken(NameTokens.basicstudywizard).build());
        String link = "/login?url=" + studyWizardLink;
        if (isLoggedIn) {
            link = "#" + studyWizardLink;
        }
        wizardLink.setHref(link);

    }

    @Override
    public void setStatValue(AppStatProxy.STAT stat, long value) {
        statMap.get(stat).setInnerText(String.valueOf(value));
    }

    @Override
    public HasData<NewsItemProxy> getNewsDisplay() {
        return newsList;
    }

    @Override
    public void displayHistogram(List<DateStatHistogramProxy> histogram, DateStatHistogramFacetProxy.TYPE chartType, DateStatHistogramProxy.INTERVAL interval) {
        String label = getLabelFromChartType(chartType);
        recentDataChart.draw(getDataTableFromHistogram(histogram, label, interval), getChartOptions(interval));
        chartTypeLb.setInnerText(label);
        selectChartIntervalType(interval);

    }

    private void selectChartIntervalType(DateStatHistogramProxy.INTERVAL interval) {
       /* weekBtn.setActive(false);

        yearBtn.setActive(false);
        monthBtn.setActive(false);
        switch (interval) {
            case WEEK:
                weekBtn.setActive(true);
                break;
            case MONTH:
                monthBtn.setActive(true);
                break;
            case YEAR:
                yearBtn.setActive(true);
                break;
        }    */
    }

    private String getLabelFromChartType(DateStatHistogramFacetProxy.TYPE chartType) {
        String label = "";
        // studyTypeBtn.setActive(false);
        // analysisBtn.setActive(false);
        // phenotypeBtn.setActive(false);
        switch (chartType) {
            case study:
                label = "Studies";
                //      studyTypeBtn.setActive(true);
                break;
            case phenotype:
                label = "Phenotypes";
                //     phenotypeBtn.setActive(true);
                break;
            case analysis:
                label = "Analyses";
                //     analysisBtn.setActive(true);
                break;
        }
        return label;
    }

    private LineChartOptions getChartOptions(DateStatHistogramProxy.INTERVAL interval) {
        LineChartOptions options = LineChartOptions.create();
        Animation animation = Animation.create();
        animation.setEasing(AnimationEasing.IN_AND_OUT);
        animation.setDuration(1000);
        options.setAnimation(animation);
        options.setHeight(350);
        HAxis haxis = HAxis.create();
        haxis.setTitle(interval.name());
        VAxis vaxis = VAxis.create();
        vaxis.setTitle("COUNT");
        options.setVAxis(vaxis);
        options.setCurveType(CurveType.FUNCTION);
        switch (interval) {
            case WEEK:
                haxis.setFormat("d MMM y");
                break;
            case MONTH:
                haxis.setFormat("MMM y");
                break;
            case YEAR:
                haxis.setFormat("y");
                break;
        }
        options.setHAxis(haxis);
        return options;
    }


    private DataTable getDataTableFromHistogram(List<DateStatHistogramProxy> histogram, String label, DateStatHistogramProxy.INTERVAL interval) {
        if (histogram == null)
            return null;
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.DATE, interval.name());
        dataTable.addColumn(ColumnType.NUMBER, label);
        dataTable.addRows(histogram.size());
        for (int i = 0; i < histogram.size(); i++) {
            DateStatHistogramProxy stat = histogram.get(i);
            dataTable.setValue(i, 0, stat.getTime());
            dataTable.setValue(i, 1, stat.getValue());
        }
        DateFormatOptions formatOptions = DateFormatOptions.create();
        switch (interval) {
            case WEEK:
                formatOptions.setPattern("d MMM y");
                break;
            case MONTH:
                formatOptions.setPattern("MMM y");
                break;
            case YEAR:
                formatOptions.setPattern("y");
                break;
        }
        DateFormat format = DateFormat.create(formatOptions);
        format.format(dataTable, 0);
        return dataTable;
    }

    @UiHandler("tourBtn")
    public void onClickTour(ClickEvent e) {
        startTour();
    }

    private void startTour() {
        GwtTour.startTour(tour, 0);
    }

    @UiHandler("studyTypeBtn")
    public void onClickStudyBtn(ClickEvent e) {
        getUiHandlers().onChangeChartType(DateStatHistogramFacetProxy.TYPE.study);
    }

    @UiHandler("phenotypeBtn")
    public void onClickPhenotypeBtn(ClickEvent e) {
        getUiHandlers().onChangeChartType(DateStatHistogramFacetProxy.TYPE.phenotype);
    }

    @UiHandler("analysisBtn")
    public void onClickAnalysisBtn(ClickEvent e) {
        getUiHandlers().onChangeChartType(DateStatHistogramFacetProxy.TYPE.analysis);
    }

    @UiHandler("weekBtn")
    public void onClickWeekBtn(ClickEvent e) {
        getUiHandlers().onChangeChartInterval(DateStatHistogramProxy.INTERVAL.WEEK);
    }

    @UiHandler("monthBtn")
    public void onClickMonthBtn(ClickEvent e) {
        getUiHandlers().onChangeChartInterval(DateStatHistogramProxy.INTERVAL.MONTH);
    }

    @UiHandler("yearBtn")
    public void onClickYearBtn(ClickEvent e) {
        getUiHandlers().onChangeChartInterval(DateStatHistogramProxy.INTERVAL.YEAR);
    }


}
