package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface ExperimentListDataGridColumns {


    public static class NameColumn extends HyperlinkPlaceManagerColumn<ExperimentProxy> {

        private final PlaceRequest.Builder placeRequest;

        public NameColumn(final PlaceManager placeManager, final PlaceRequest.Builder placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(ExperimentProxy object) {
            String name = object.getName();
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()).build());
            return new HyperlinkPlaceManagerColumn.HyperlinkParam(name, url);
        }
    }

    public static class DesignColumn extends Column<ExperimentProxy, String> {
        public DesignColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(ExperimentProxy object) {
            String retval = object.getDesign();
            if (retval != null && retval.length() > 145)
                retval = retval.substring(0, 145) + "...";
            return retval;
        }
    }

    public static class OriginatorColumn extends Column<ExperimentProxy, String> {

        public OriginatorColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(ExperimentProxy object) {
            return object.getOriginator();
        }
    }

    public static class CommentsColumn extends Column<ExperimentProxy, String> {
        public CommentsColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(ExperimentProxy object) {
            return object.getComments();
        }
    }

    public class TitleColumn extends IdentityColumn<ExperimentProxy> {
        public TitleColumn(PlaceManager placeManager, PlaceRequest.Builder requestBuilder) {
            super(new TitleCell(requestBuilder, placeManager));
        }
    }

    public static class TitleCell extends AbstractCell<ExperimentProxy> {

        interface Template extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<div style=\"font-size:110%;\"><a href=\"{0}\">{1}</a></div><div style=\"font-size:90%;color:#777;\">{2}</div>")
            SafeHtml cell(SafeUri link, SafeHtml name, SafeHtml subTitle);

        }

        private static Template templates = GWT.create(Template.class);

        private final PlaceManager placeManager;
        private PlaceRequest.Builder placeRequest;

        public TitleCell(PlaceRequest.Builder placeRequest, PlaceManager placeManager) {
            super();
            this.placeManager = placeManager;
            this.placeRequest = placeRequest;
        }

        @Override
        public void render(Context context, ExperimentProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            placeRequest.with("id", value.getId().toString());
            SafeUri link = UriUtils.fromTrustedString("#" + placeManager.buildHistoryToken(placeRequest.build()));
            SafeHtml name = SafeHtmlUtils.fromString(value.getName());
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder
                    .append(SafeHtmlUtils.fromSafeConstant("created on "))
                    .append(SafeHtmlUtils.fromString(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(value.getCreated())))
                    .append(SafeHtmlUtils.fromSafeConstant(" by "))
                    .append(SafeHtmlUtils.fromString(value.getOriginator()));
            sb.append(templates.cell(link, name, builder.toSafeHtml()));
        }
    }
}
