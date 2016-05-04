package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import com.arcbees.chosen.client.ChosenImpl;
import com.arcbees.chosen.client.ChosenOptions;
import com.arcbees.chosen.client.ResultsFilter;
import com.arcbees.chosen.client.SelectParser;
import com.arcbees.chosen.client.event.ChosenChangeEvent;
import com.arcbees.chosen.client.gwt.ChosenListBox;
import com.github.timeu.dygraphsgwt.client.callbacks.Point;
import com.github.timeu.gwtlibs.gwasviewer.client.DisplayFeature;
import com.github.timeu.gwtlibs.gwasviewer.client.GWASViewer;
import com.github.timeu.gwtlibs.gwasviewer.client.SettingsPanel;
import com.github.timeu.gwtlibs.gwasviewer.client.Track;
import com.github.timeu.gwtlibs.gwasviewer.client.events.FilterChangeEvent;
import com.github.timeu.gwtlibs.gwasviewer.client.events.GeneDataSource;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.ui.PlotDownloadPopup;
import com.gmi.nordborglab.browser.client.ui.ResizeableFlowPanel;
import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.google.common.base.Optional;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.googlecode.gwt.charts.client.DataTable;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/26/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASPlotView extends ViewWithUiHandlers<GWASPlotUiHandlers> implements GWASPlotPresenterWidget.MyView {

    interface Binder extends UiBinder<Widget, GWASPlotView> {

    }

    class GWASFilterChangeHandler implements FilterChangeEvent.Handler {

        private GWASViewer gwasViewer;

        GWASFilterChangeHandler(GWASViewer gwasViewer) {
            this.gwasViewer = gwasViewer;
        }

        @Override
        public void onFilterChanged(FilterChangeEvent event) {
            SettingsPanel.MINOR_FILTER filterType = gwasViewer.getMinorFilterType();
            for (GWASViewer viewer : gwasGeneViewers) {
                if (viewer != gwasViewer) {

                    viewer.setMinorFilterType(filterType);
                    viewer.setMinMAC(gwasViewer.getMinMAC());
                    viewer.setMinMAF(gwasViewer.getMinMAF());
                    viewer.filterAndDraw();
                }
            }
        }
    }

    private final Widget widget;
    private String[] colors = {"blue", "green", "red", "cyan", "purple"};
    private String[] gene_mark_colors = {"red", "red", "blue", "red", "green"};
    protected List<GWASViewer> gwasGeneViewers = new ArrayList<>();
    protected List<FilterChangeEvent.Handler> filterChangeHandlers = new ArrayList<>();
    private final GeneDataSource geneDataSource;
    private Track[] tracks;
    private final Modal popUpPanel = new Modal();
    private final PlotDownloadPopup plotPanel = new PlotDownloadPopup();

    public interface SearchGeneCallback {
        void onDisplayResults(List<SearchFacetPageProxy> results);
    }

    private class GeneResultFilter implements ResultsFilter {

        @Override
        public void filter(String searchString, ChosenImpl chosen, boolean isShowing) {
            if (!isShowing)
                return;
            getUiHandlers().onSearchGenes(searchString, results -> {
                List<SelectParser.SelectItem> selectItems = chosen.getSelectItems();
                selectItems.clear();
                int arrayIndex = 0;
                int groupIndex = 0;
                for (SearchFacetPageProxy category : results) {
                    SelectParser.GroupItem group = new SelectParser.GroupItem();
                    group.setLabel(category.getCategory().name());
                    group.setDomId("geneGroup_" + arrayIndex);
                    group.setChildren(category.getContents().size());
                    selectItems.add(group);
                    arrayIndex++;
                    for (SearchItemProxy item : category.getContents()) {
                        SelectParser.OptionItem optionItem = new SelectParser.OptionItem();
                        optionItem.setHtml("<div>" + item.getDisplayText() + "</dv>");
                        optionItem.setText(item.getDisplayText());
                        optionItem.setValue(category.getCategory().name() + "__" + item.getId());
                        optionItem.setArrayIndex(arrayIndex);
                        optionItem.setOptionsIndex(arrayIndex);
                        optionItem.setGroupArrayIndex(groupIndex);
                        optionItem.setDomId("geneOption_" + arrayIndex++);
                        selectItems.add(optionItem);
                    }
                    groupIndex++;
                }

                chosen.rebuildResultItems();
            });
        }
    }

    @UiField
    ResizeableFlowPanel container;
    @UiField
    Button downloadBtn;
    @UiField(provided = true)
    ChosenListBox geneSearchBox;
    @UiField
    LayoutPanel panel;

    @Inject
    public GWASPlotView(final Binder binder, final GeneDataSource geneDataSource) {
        ChosenOptions options = new ChosenOptions();
        options.setSingleBackstrokeDelete(true);
        options.setNoResultsText("No user found");
        options.setResultFilter(new GeneResultFilter());
        geneSearchBox = new ChosenListBox(true, options);
        geneSearchBox.setWidth("100%");
        widget = binder.createAndBindUi(this);
        // avoid horizontal scroll bars due to gene info popup
        widget.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        this.geneDataSource = geneDataSource;
        ModalBody modalBody = new ModalBody();
        modalBody.add(plotPanel);
        popUpPanel.add(modalBody);
        popUpPanel.setTitle("Download GWAS Plots");
        geneSearchBox.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void drawGWASPlots(GWASDataDTO gwasData) {
        geneSearchBox.forceRedraw();
        Integer i = 1;
        java.util.Iterator<DataTable> iterator = gwasData.getGwasDataTables().iterator();
        while (iterator.hasNext()) {
            GWASViewer chart = null;
            DataTable dataTable = iterator.next();
            String[] color = new String[]{colors[i % colors.length]};
            String gene_marker_color = gene_mark_colors[i % gene_mark_colors.length];
            if (gwasGeneViewers.size() >= i)
                chart = gwasGeneViewers.get((i - 1));
            if (chart == null) {
                chart = new GWASViewer(gwasData.getChromosomes().get(i - 1), color, gene_marker_color, geneDataSource);
                chart.setMinorFilterType(SettingsPanel.MINOR_FILTER.MAC);
                // TODO activate later
                chart.setUploadTrackWidget(null);
                if (tracks != null) {
                    chart.setTracks(tracks);
                }
                GWASFilterChangeHandler filterChangeHandler = new GWASFilterChangeHandler(chart);
                chart.addFilterChangeHandler(filterChangeHandler);
                filterChangeHandlers.add(filterChangeHandler);
                gwasGeneViewers.add(chart);
                chart.setGeneInfoUrl("http://arabidopsis.org/servlets/TairObject?name={0}&type=gene");
                container.add((IsWidget) chart);

                chart.addSelectTrackHandler(event -> {
                    GWASViewer viewer = (GWASViewer) event.getSource();
                    getUiHandlers().onLoadTrackData(event.getId(), event.isStacked(), viewer.getChromosome());
                });

                chart.addPointClickHandler(event -> {
                    Point point = event.point;
                    NativeEvent mouseEvent = event.event;
                    GWASViewer gwasViewer = (GWASViewer) event.getSource();
                    String id = gwasViewer.getChromosome();
                    int chromosome;
                    try {
                        chromosome = Integer.parseInt(id);
                    } catch (Exception e) {
                        chromosome = Integer.parseInt(id.charAt(3) + "");
                    }
                    getUiHandlers().onSelectSNP(chromosome, (int) point.getXval(), mouseEvent.getClientX(), mouseEvent.getClientY());
                });
            }
            chart.draw(dataTable, gwasData.getMaxScore(), gwasData.getBonferroniThreshold(), gwasData.getChrLengths().get(i - 1));
            chart.clearDisplayFeatures();
            chart.clearSelection();
            i++;
        }
    }


    @Override
    public void setTracks(Track[] tracks) {
        if (this.tracks == null) {
            this.tracks = tracks;
            for (int i = 0; i < container.getWidgetCount(); i++) {
                GWASViewer viewer = (GWASViewer) container.getWidget(i);
                viewer.setTracks(tracks);
            }
        }
    }

    @Override
    public void setTrackData(String id, DataTable data, boolean isStacked, String chr) {
        Optional<GWASViewer> viewer = getViewerFromChr(chr);
        if (viewer.isPresent()) {
            viewer.get().setTrackData(id, isStacked, data);
        }
    }

    private Optional<GWASViewer> getViewerFromChr(String chr) {
        GWASViewer viewer = null;
        for (int i = 0; i < container.getWidgetCount(); i++) {
            GWASViewer v = (GWASViewer) container.getWidget(i);
            if (v.getChromosome().equalsIgnoreCase(chr)) {
                viewer = v;
                break;
            }
        }
        return Optional.fromNullable(viewer);
    }

    @UiHandler("downloadBtn")
    public void onClickDownloadBtn(ClickEvent e) {
        popUpPanel.show();

    }

    @Override
    public void setPlotSettings(Long id, GetGWASDataAction.TYPE type) {
        plotPanel.setSettings(id, type);
    }

    @Override
    public void removeDisplayFeaturesFromGWAS(int chr, Collection<DisplayFeature> features) {
        GWASViewer viewer = (GWASViewer) container.getWidget(chr - 1);
        viewer.removeDisplayFeatures(features);
    }

    @Override
    public void addDisplayFeaturesToGWAS(int chr, Collection<DisplayFeature> features) {
        GWASViewer viewer = (GWASViewer) container.getWidget(chr - 1);
        for (DisplayFeature feature : features) {
            viewer.addDisplayFeature(feature, true);
        }
        viewer.refresh();
    }

    @UiHandler("geneSearchBox")
    public void onSelectGene(ChosenChangeEvent e) {
        getUiHandlers().onHighlightGene(e.getValue(), e.isSelection());
    }

}