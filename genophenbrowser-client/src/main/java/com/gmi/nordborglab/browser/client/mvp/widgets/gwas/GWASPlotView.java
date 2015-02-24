package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import at.gmi.nordborglab.widgets.geneviewer.client.datasource.DataSource;
import at.gmi.nordborglab.widgets.gwasgeneviewer.client.GWASGeneViewer;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.ui.ResizeableFlowPanel;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataTable;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.danvk.dygraphs.client.events.DataPoint;
import org.danvk.dygraphs.client.events.SelectHandler;

import java.util.ArrayList;
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

    class GWASFilterChangeHandler implements GWASGeneViewer.FilterChangeHandler {

        private GWASGeneViewer gwasViewer;

        GWASFilterChangeHandler(GWASGeneViewer gwasViewer) {
            this.gwasViewer = gwasViewer;
        }

        @Override
        public void onChange() {
            GWASGeneViewer.MINOR_FILTER filterType = gwasViewer.getFilterType();
            double value = 0.0;
            switch (filterType) {
                case MAC:
                    value = gwasViewer.getMinMAC();
                    break;
                case MAF:
                    value = gwasViewer.getMinMAF();
                    break;
            }
            for (GWASGeneViewer viewer : gwasGeneViewers) {
                if (viewer != gwasViewer) {
                    viewer.setFilterType(filterType);
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
    protected List<GWASGeneViewer> gwasGeneViewers = new ArrayList<GWASGeneViewer>();
    protected List<GWASGeneViewer.FilterChangeHandler> filterChangeHandlers = new ArrayList<GWASGeneViewer.FilterChangeHandler>();
    private final DataSource geneDataSource;

    @UiField
    ResizeableFlowPanel container;

    @Inject
    public GWASPlotView(final Binder binder, final DataSource geneDataSource) {
        widget = binder.createAndBindUi(this);
        this.geneDataSource = geneDataSource;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void drawGWASPlots(GWASDataDTO gwasData) {
        Integer i = 1;
        java.util.Iterator<DataTable> iterator = gwasData.getGwasDataTables().iterator();
        while (iterator.hasNext()) {
            GWASGeneViewer chart = null;
            DataTable dataTable = iterator.next();
            String[] color = new String[]{colors[i % colors.length]};
            String gene_marker_color = gene_mark_colors[i % gene_mark_colors.length];
            if (gwasGeneViewers.size() >= i)
                chart = gwasGeneViewers.get((i - 1));
            if (chart == null) {
                chart = new GWASGeneViewer("Chr" + i.toString(), color, gene_marker_color, geneDataSource, null);
                GWASFilterChangeHandler filterChangeHandler = new GWASFilterChangeHandler(chart);
                chart.setFilterChangeHandler(filterChangeHandler);
                filterChangeHandlers.add(filterChangeHandler);
                gwasGeneViewers.add(chart);
                chart.setGeneInfoUrl("http://arabidopsis.org/servlets/TairObject?name={0}&type=gene");
                container.add((IsWidget) chart);
                chart.addSelectionHandler(new SelectHandler() {

                    @Override
                    public void onSelect(SelectEvent event) {
                        DataPoint point = event.point;
                        Event mouseEvent = event.event;
                        String id = event.id;
                        int chromosome;
                        try {
                            chromosome = Integer.parseInt(id);
                        } catch (Exception e) {
                            chromosome = Integer.parseInt(id.charAt(3) + "");
                        }
                        getUiHandlers().onSelectSNP(chromosome, (int) point.getXVal(), mouseEvent.getClientX(), mouseEvent.getClientY());
                    }

                });
            }
            chart.clearDisplayGenes();
            chart.clearSelection();
            chart.draw(dataTable, gwasData.getMaxScore(), 0, gwasData.getChrLengths().get(i - 1), gwasData.getBonferroniThreshold());
            chart.onResize();
            i++;
        }
    }
}