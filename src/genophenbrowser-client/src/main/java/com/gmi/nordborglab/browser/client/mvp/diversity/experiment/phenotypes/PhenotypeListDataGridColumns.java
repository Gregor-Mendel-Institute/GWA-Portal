package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.phenotypes;

import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
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
import com.google.gwt.user.cellview.client.TextColumn;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public interface PhenotypeListDataGridColumns {

    public static class NameColumn extends HyperlinkPlaceManagerColumn<PhenotypeProxy> {

        private final PlaceRequest.Builder placeRequest;


        public NameColumn(final PlaceManager placeManager, final PlaceRequest.Builder placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkParam getValue(PhenotypeProxy object) {
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()).build());
            String name = object.getLocalTraitName();
            return new HyperlinkParam(name, url);
        }

    }

    public static class ProtocolColumn extends Column<PhenotypeProxy, String> {

        public ProtocolColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(PhenotypeProxy object) {
            return object.getTraitProtocol();
        }

    }

    public static class TraitOntologyColumn extends Column<PhenotypeProxy, TermProxy> {

        public TraitOntologyColumn() {
            super(new TermCell());
        }

        @Override
        public TermProxy getValue(PhenotypeProxy object) {
            return object.getTraitOntologyTerm();
        }
    }

    public static class EnvironOntologyColumn extends Column<PhenotypeProxy, TermProxy> {

        public EnvironOntologyColumn() {
            super(new TermCell());
        }

        @Override
        public TermProxy getValue(PhenotypeProxy object) {
            return object.getEnvironOntologyTerm();
        }
    }

    public static class ExperimentColumn extends TextColumn<PhenotypeProxy> {

        @Override
        public String getValue(PhenotypeProxy object) {
            String experiment = "";
            if (object.getExperiment() != null) {
                experiment = object.getExperiment().getName();
            }
            return experiment;
        }
    }

    public class TitleColumn extends IdentityColumn<PhenotypeProxy> {
        public TitleColumn(PlaceManager placeManager, PlaceRequest.Builder requestBuilder) {
            super(new TitleCell(requestBuilder, placeManager));
        }
    }

    public static class TitleCell extends AbstractCell<PhenotypeProxy> {

        interface Template extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<div style=\"font-size:110%;\"><a href=\"{0}\">{1}</a></div><div style=\"font-size:90%;color:#777;\">{2}</div>")
            SafeHtml cell(SafeUri link, SafeHtml name, SafeHtml subTitle);

        }

        private static Template templates = GWT.create(Template.class);

        private final PlaceManager placeManager;
        private PlaceRequest.Builder requestBuilder;

        public TitleCell(PlaceRequest.Builder requestBuilder, PlaceManager placeManager) {
            super();
            this.placeManager = placeManager;
            this.requestBuilder = requestBuilder;
        }

        @Override
        public void render(Context context, PhenotypeProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            requestBuilder.with("id", value.getId().toString());
            SafeUri link = UriUtils.fromTrustedString("#" + placeManager.buildHistoryToken(requestBuilder.build()));
            SafeHtml name = SafeHtmlUtils.fromString(value.getLocalTraitName());
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder
                    .append(SafeHtmlUtils.fromSafeConstant("created on "))
                    .append(SafeHtmlUtils.fromString(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(value.getCreated())))
                    .append(SafeHtmlUtils.fromSafeConstant(" by "))
                    .append(SafeHtmlUtils.fromString(getCreator(value.getOwnerUser())));
            sb.append(templates.cell(link, name, builder.toSafeHtml()));
        }

        private static String getCreator(AppUserProxy user) {
            if (user == null)
                return "";
            StringBuilder builder = new StringBuilder();
            if (user.getFirstname() != "")
                builder.append(user.getFirstname());
            builder.append(" ");
            if (user.getLastname() != null)
                builder.append(user.getLastname());
            return builder.toString();

        }
    }

    public static class TermCell extends AbstractCell<TermProxy> {

        interface Template extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<div style=\"font-size:110%;\">{0}</div><div style=\"font-size:90%;color:#777;\">{1}</div>")
            SafeHtml cell(SafeHtml name, SafeHtml subTitle);

        }

        private static Template templates = GWT.create(Template.class);

        public TermCell() {
            super();
        }

        @Override
        public void render(Context context, TermProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            SafeHtml name = SafeHtmlUtils.fromString(value.getName());
            SafeHtml title = SafeHtmlUtils.fromString(value.getAcc());
            sb.append(templates.cell(name, title));
        }
    }


}
