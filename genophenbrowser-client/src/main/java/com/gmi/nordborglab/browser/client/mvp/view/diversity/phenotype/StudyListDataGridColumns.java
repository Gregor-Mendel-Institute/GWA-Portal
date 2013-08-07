package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.client.ui.cells.LabelTypeCell;
import com.gmi.nordborglab.browser.client.ui.cells.ProgressBarCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.Date;
import java.util.List;

public interface StudyListDataGridColumns {

    public static class NameColumn extends HyperlinkPlaceManagerColumn<StudyProxy> {

        private final PlaceRequest placeRequest;


        public NameColumn(final PlaceManager placeManager, final PlaceRequest placeRequest) {
            super(new HyperlinkCell(), placeManager);
            this.placeRequest = placeRequest;
        }

        @Override
        public HyperlinkParam getValue(StudyProxy object) {
            String url = "#" + placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
            String name = object.getName();
            return new HyperlinkParam(name, url);
        }

    }

    public static class ProducerColumn extends Column<StudyProxy, String> {

        public ProducerColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(StudyProxy object) {
            return object.getProducer();
        }

    }

    public static class StudyDateColumn extends Column<StudyProxy, Date> {

        public StudyDateColumn() {
            super(new DateCell());
        }

        @Override
        public Date getValue(StudyProxy object) {
            return object.getStudyDate();
        }

    }

    public static class AlleleAssayColumn extends Column<StudyProxy, String> {

        public AlleleAssayColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(StudyProxy object) {
            String assay = null;
            if (object.getAlleleAssay() != null)
                assay = object.getAlleleAssay().getName();
            return assay;
        }

    }

    public static class ProtocolColumn extends Column<StudyProxy, String> {

        public ProtocolColumn() {
            super(new TextCell());
        }

        @Override
        public String getValue(StudyProxy object) {
            if (object.getProtocol() == null)
                return null;
            return object.getProtocol().getAnalysisMethod();
        }

    }

    public static class PhenotypeColumn extends TextColumn<StudyProxy> {
        @Override
        public String getValue(StudyProxy object) {
            String phenotype = null;
            if (object.getPhenotype() != null)
                phenotype = object.getPhenotype().getLocalTraitName();
            return phenotype;
        }
    }

    public static class ExperimentColumn extends TextColumn<StudyProxy> {
        @Override
        public String getValue(StudyProxy object) {
            String experiment = null;
            if (object.getPhenotype().getExperiment() != null)
                experiment = object.getPhenotype().getExperiment().getName();
            return experiment;
        }
    }

    public static class ProgressCell implements HasCell<StudyJobProxy, Number> {

        @Override
        public Cell<Number> getCell() {
            return new ProgressBarCell(true, true, null);
        }

        @Override
        public FieldUpdater<StudyJobProxy, Number> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Number getValue(StudyJobProxy object) {
            if (object != null) {
                return object.getProgress();
            } else {
                return null;
            }
        }
    }

    public static class TransformationColumn extends TextColumn<StudyProxy> {

        @Override
        public String getValue(StudyProxy object) {
            String transformation = null;
            if (object.getTransformation() != null) {
                transformation = object.getTransformation().getName();
            }
            return transformation;
        }
    }

    public static class StatusCell implements HasCell<StudyJobProxy, String> {

        @Override
        public Cell<String> getCell() {
            return new LabelTypeCell(
                    ImmutableMap.<String, LabelType>builder()
                            .put("Finished", LabelType.SUCCESS)
                            .put("Running", LabelType.WARNING)
                            .put("Queued", LabelType.IMPORTANT)
                            .put("Error", LabelType.IMPORTANT).build()
            );
        }

        @Override
        public FieldUpdater<StudyJobProxy, String> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getValue(StudyJobProxy object) {
            if (object != null) {
                return object.getStatus();
            }
            return "N/A";
        }
    }


    public static class StatusCompositeCell extends CompositeCell<StudyJobProxy> {


        public StatusCompositeCell(List<HasCell<StudyJobProxy, ?>> hasCells) {
            super(hasCells);
        }

        @Override
        protected <X> void render(Context context, StudyJobProxy value, SafeHtmlBuilder sb, HasCell<StudyJobProxy, X> hasCell) {
            final Cell<X> cell = hasCell.getCell();
            sb.appendHtmlConstant("<div style=\"display:inline-block;margin-right:20px;\">");
            if (!(cell instanceof ProgressBarCell && value != null && (value.getStatus().equalsIgnoreCase("Finished") || value.getStatus().equalsIgnoreCase("Error")))) {
                cell.render(context, hasCell.getValue(value), sb);
            }
            sb.appendHtmlConstant("</div>");
        }
    }


    public static class StatusColumn extends Column<StudyProxy, StudyJobProxy> {

        public StatusColumn(List<HasCell<StudyJobProxy, ?>> cells) {
            super(new StatusCompositeCell(cells));
        }

        @Override
        public StudyJobProxy getValue(StudyProxy object) {
            return object.getJob();
        }
    }

    public class TitleColumn extends IdentityColumn<StudyProxy> {
        public TitleColumn(PlaceManager placeManager, PlaceRequest request) {
            super(new TitleCell(request, placeManager));
        }
    }

    public static class TitleCell extends AbstractCell<StudyProxy> {

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
        public void render(Context context, StudyProxy value, SafeHtmlBuilder sb) {
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
