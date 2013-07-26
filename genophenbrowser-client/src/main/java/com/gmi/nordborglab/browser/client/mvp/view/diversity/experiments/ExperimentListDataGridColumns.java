package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.github.gwtbootstrap.client.ui.TooltipCellDecorator;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.client.ui.cells.LabelTypeCell;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface ExperimentListDataGridColumns {


    public static class NameColumn extends HyperlinkPlaceManagerColumn<ExperimentProxy> {

        private final PlaceRequest placeRequest;

        public NameColumn(final PlaceManager placeManager, final PlaceRequest placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(ExperimentProxy object) {
            String name = object.getName();
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
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
        public TitleColumn(PlaceManager placeManager, ParameterizedPlaceRequest parameterizedPlaceRequest) {
            super(new TitleCell(parameterizedPlaceRequest, placeManager));
        }
    }

    public static class TitleCell extends AbstractCell<ExperimentProxy> {

        interface Template extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<div style=\"font-size:110%;\"><a href=\"{0}\">{1}</a></div><div style=\"font-size:90%;color:#777;\">{2}</div>")
            SafeHtml cell(SafeUri link, SafeHtml name, SafeHtml subTitle);

        }

        private static Template templates = GWT.create(Template.class);

        private final PlaceManager placeManager;
        private PlaceRequest placeRequest;

        public TitleCell(PlaceRequest placeRequest, PlaceManager placeManager) {
            super();
            this.placeManager = placeManager;
            this.placeRequest = placeRequest;
        }

        @Override
        public void render(Context context, ExperimentProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            placeRequest.with("id", value.getId().toString());
            SafeUri link = UriUtils.fromTrustedString("#" + placeManager.buildHistoryToken(placeRequest));
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
