package com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport;

import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.ui.cells.FlagCell;
import com.gmi.nordborglab.browser.client.ui.cells.HighlightColumn;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.Set;

public interface PassportDataGridColumns {


    public abstract static class PassportHighlightColumn extends HighlightColumn<PassportProxy> {


        public PassportHighlightColumn(final SearchTerm searchTerm) {
            super(searchTerm);
        }
    }


    public static class IdColumn extends HyperlinkPlaceManagerColumn<PassportProxy> {

        private final PlaceRequest.Builder placeRequest;

        public IdColumn(final PlaceManager placeManager, final PlaceRequest.Builder placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkParam getValue(PassportProxy object) {
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()).build());
            String name = object.getId().toString();
            return new HyperlinkParam(name, url);
        }
    }

    public static class AccNameColumn extends PassportHighlightColumn {

        public AccNameColumn(SearchTerm searchTerm) {
            super(searchTerm);
        }

        @Override
        public String getValue(PassportProxy object) {
            return object.getAccename();
        }
    }

    public static class CountryColumn extends Column<PassportProxy, String> {

        public CountryColumn(FlagMap map) {
            super(new FlagCell(map));
        }

        @Override
        public String getValue(PassportProxy object) {
            String icon = null;
            try {
                icon = object.getCollection().getLocality().getOrigcty();
            } catch (Exception e) {

            }
            return icon;
        }
    }

    public static class CollectorColumn extends PassportHighlightColumn {

        public CollectorColumn(SearchTerm searchTerm) {
            super(searchTerm);
        }

        @Override
        public String getValue(PassportProxy object) {
            String collector = "";
            try {
                collector = object.getCollection().getCollector();
            } catch (Exception e) {

            }
            return collector;
        }
    }

    public static class CollDateColumn extends Column<PassportProxy, String> {

        public CollDateColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(PassportProxy object) {
            String collDate = null;
            try {
                collDate = object.getCollection().getCollDate();
            } catch (Exception e) {

            }
            return collDate;
        }
    }

    public static class TypeColumn extends Column<PassportProxy, String> {

        public TypeColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(PassportProxy object) {
            String sampStat = "";
            try {
                sampStat = object.getSampstat().getGermplasmType();
            } catch (Exception e) {
            }
            return sampStat;
        }
    }

    public static class SourceColumn extends PassportHighlightColumn {

        public SourceColumn(SearchTerm searchTerm) {
            super(searchTerm);
        }

        @Override
        public String getValue(PassportProxy object) {
            String source = "";
            try {

                source = object.getSource().getSource();
            } catch (Exception e) {
            }
            return source;
        }
    }

    public static class AccNumberColumn extends PassportHighlightColumn {

        public AccNumberColumn(SearchTerm searchTerm) {
            super(searchTerm);
        }

        @Override
        public String getValue(PassportProxy object) {
            return object.getAccenumb();
        }
    }


    public static class AlleleAssayRenderer extends AbstractSafeHtmlRenderer<Set<AlleleAssayProxy>> {

        interface Template extends SafeHtmlTemplates {

            @Template("<li >{0}</li>")
            SafeHtml item(String text);
        }

        private static Template template = GWT.create(Template.class);
        private static AlleleAssayRenderer instance;

        public static AlleleAssayRenderer getInstance() {
            if (instance == null) {
                instance = new AlleleAssayRenderer();
            }
            return instance;
        }

        @Override
        public SafeHtml render(Set<AlleleAssayProxy> object) {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendHtmlConstant("<ul style=\"\">");
            for (AlleleAssayProxy alleleAssayProxy : object) {
                builder.append(template.item(alleleAssayProxy.getName()));
            }
            builder.appendHtmlConstant("</ul>");
            return builder.toSafeHtml();
        }
    }

    public static class AlleleAssayCell extends AbstractSafeHtmlCell<Set<AlleleAssayProxy>> {

        public AlleleAssayCell() {
            super(AlleleAssayRenderer.getInstance());
        }

        @Override
        protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
            sb.append(data);
        }

    }


    public static class AlleleAssayColumn extends Column<PassportProxy, Set<AlleleAssayProxy>> {

        public AlleleAssayColumn() {
            super(new AlleleAssayCell());
            // TODO Auto-generated constructor stub
        }

        @Override
        public Set<AlleleAssayProxy> getValue(PassportProxy object) {
            return object.getAlleleAssays();
        }

    }
}
