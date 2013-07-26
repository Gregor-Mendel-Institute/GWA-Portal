package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.client.ui.cells.LabelTypeCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface PhenotypeListDataGridColumns {

    public static class NameColumn extends HyperlinkPlaceManagerColumn<PhenotypeProxy> {

        private final PlaceRequest placeRequest;


        public NameColumn(final PlaceManager placeManager, final PlaceRequest placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkParam getValue(PhenotypeProxy object) {
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
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

    public static class TraitOntologyColumn extends Column<PhenotypeProxy, String> {

        public TraitOntologyColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(PhenotypeProxy object) {
            String to = "";
            if (object.getTraitOntologyTerm() != null) {
                to = object.getTraitOntologyTerm().getName() + " (" + object.getTraitOntologyTerm().getAcc() + ")";
            }
            return to;
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
        public TitleColumn(PlaceManager placeManager, ParameterizedPlaceRequest parameterizedPlaceRequest) {
            super(new TitleCell(parameterizedPlaceRequest, placeManager));
        }
    }

    public static class TitleCell extends AbstractCell<PhenotypeProxy> {

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
        public void render(Context context, PhenotypeProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            placeRequest.with("id", value.getId().toString());
            SafeUri link = UriUtils.fromTrustedString("#" + placeManager.buildHistoryToken(placeRequest));
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
}
