package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.detail;

import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.studies.StudyListDataGridColumns;
import com.gmi.nordborglab.browser.client.ui.cells.BarCell;
import com.gmi.nordborglab.browser.client.ui.cells.LabelTypeCell;
import com.gmi.nordborglab.browser.client.ui.cells.ProgressBarCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.12.13
 * Time: 15:31
 * To change this template use File | Settings | File Templates.
 */
public interface CandidateGeneListEnrichmentDataGridColumns {

    public static class PValueBarHasCell implements HasCell<CandidateGeneListEnrichmentProxy, Number> {

        private double maxPvalue = 0;

        public PValueBarHasCell() {
        }

        @Override
        public Cell<Number> getCell() {
            return new BarCell();
        }

        @Override
        public FieldUpdater<CandidateGeneListEnrichmentProxy, Number> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Number getValue(CandidateGeneListEnrichmentProxy object) {
            double percentage = 0;
            if (maxPvalue > 0 && object.getPvalue() != null && object.getPvalue() != 0) {
                percentage = (1 - object.getPvalue() / maxPvalue) * 100 - 20;
            }
            return percentage;
        }

        public void setMaxPvalue(double maxPvalue) {
            this.maxPvalue = maxPvalue;
        }

        public double getMaxPvalue() {
            return maxPvalue;
        }
    }

    public static class PValueHasCell implements HasCell<CandidateGeneListEnrichmentProxy, Number> {

        @Override
        public Cell<Number> getCell() {
            return new NumberCell(NumberFormat.getDecimalFormat());
        }

        @Override
        public FieldUpdater<CandidateGeneListEnrichmentProxy, Number> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Number getValue(CandidateGeneListEnrichmentProxy object) {
            return object.getPvalue();
        }
    }


    public static class PValueCell extends CompositeCell<CandidateGeneListEnrichmentProxy> {

        public PValueCell(List<HasCell<CandidateGeneListEnrichmentProxy, ?>> hasCells) {
            super(hasCells);
        }
    }

    public static class ProgressCell implements HasCell<CandidateGeneListEnrichmentProxy, Number> {

        @Override
        public Cell<Number> getCell() {
            return new ProgressBarCell(true, true, null);
        }

        @Override
        public FieldUpdater<CandidateGeneListEnrichmentProxy, Number> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Number getValue(CandidateGeneListEnrichmentProxy object) {
            if (object != null) {
                return object.getProgress();
            } else {
                return null;
            }
        }
    }

    public static class StatusCell implements HasCell<CandidateGeneListEnrichmentProxy, String> {

        @Override
        public Cell<String> getCell() {
            return new LabelTypeCell(
                    ImmutableMap.<String, LabelType>builder()
                            .put("Finished", LabelType.SUCCESS)
                            .put("Running", LabelType.WARNING)
                            .put("Waiting", LabelType.DEFAULT)
                            .put("Error", LabelType.IMPORTANT).build()
            );
        }

        @Override
        public FieldUpdater<CandidateGeneListEnrichmentProxy, String> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getValue(CandidateGeneListEnrichmentProxy object) {
            if (object != null) {
                return object.getStatus();
            }
            return "N/A";
        }
    }


    public static class StatusCompositeCell extends CompositeCell<CandidateGeneListEnrichmentProxy> {


        public StatusCompositeCell(List<HasCell<CandidateGeneListEnrichmentProxy, ?>> hasCells) {
            super(hasCells);
        }

        @Override
        protected <X> void render(Context context, CandidateGeneListEnrichmentProxy value, SafeHtmlBuilder sb, HasCell<CandidateGeneListEnrichmentProxy, X> hasCell) {
            final Cell<X> cell = hasCell.getCell();
            sb.appendHtmlConstant("<div style=\"display:inline-block;margin-right:20px;\">");
            if (!(cell instanceof ProgressBarCell && value != null && (value.getStatus().equalsIgnoreCase("Finished") || value.getStatus().equalsIgnoreCase("Error")))) {
                cell.render(context, hasCell.getValue(value), sb);
            }
            sb.appendHtmlConstant("</div>");
        }
    }


    public static class StatusColumn extends IdentityColumn<CandidateGeneListEnrichmentProxy> {

        public StatusColumn(List<HasCell<CandidateGeneListEnrichmentProxy, ?>> cells) {
            super(new StatusCompositeCell(cells));
        }
    }


    public interface MultiCheckBoxState {
        boolean showCheckAll();

        int totalCount();

        int checkedCount();

        void setShowCheckAll(boolean showCheckAll);

        void setTotalCount(int count);

        void setCheckedCount(int count);

    }

    public class MultiCheckBoxStateImpl implements MultiCheckBoxState {

        private boolean showCheckAll = false;
        private int totalCount = 0;
        private int checkedCount = 0;

        @Override
        public boolean showCheckAll() {
            return showCheckAll;
        }

        @Override
        public int totalCount() {
            return totalCount;
        }

        @Override
        public int checkedCount() {
            return checkedCount;
        }

        @Override
        public void setShowCheckAll(boolean showCheckAll) {
            this.showCheckAll = showCheckAll;
        }

        @Override
        public void setTotalCount(int count) {
            totalCount = count;
        }

        @Override
        public void setCheckedCount(int count) {
            checkedCount = count;
        }
    }

    public static class MultiCheckBoxCell extends AbstractSafeHtmlCell<MultiCheckBoxState> {


        public MultiCheckBoxCell() {
            super(MultiCheckBoxRenderer.getInstance(), "click");
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, MultiCheckBoxState value, NativeEvent event, ValueUpdater<MultiCheckBoxState> valueUpdater) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
            if ("click".equals(event.getType())) {
                EventTarget eventTarget = event.getEventTarget();
                if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
                    if (Element.as(eventTarget) instanceof AnchorElement) {
                        if (value.checkedCount() < value.totalCount()) {
                            value.setCheckedCount(value.totalCount());
                        } else {
                            value.setShowCheckAll(false);
                        }
                        //Refresh
                        setValue(context, parent, value);
                        if (valueUpdater != null) {
                            valueUpdater.update(value);
                        }
                    }
                }
            }
        }

        @Override
        protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
            sb.append(data);
        }
    }

    public static class MultiCheckBoxRenderer extends AbstractSafeHtmlRenderer<MultiCheckBoxState> {

        interface Template extends SafeHtmlTemplates {
            @Template("All {0} records on this page are selected. <a href=\"javascript:var nop;\" >Select all <b>{1}</b> records</a>")
            SafeHtml standard(Integer count, Integer totalCount);

            @Template("All {0} records selected. <a href=\"javascript:var nop;\">Clear selection</a>")
            SafeHtml allSelected(Integer totalCount);
        }


        private static Template template = GWT.create(Template.class);
        private static MultiCheckBoxRenderer instance;

        public static MultiCheckBoxRenderer getInstance() {
            if (instance == null) {
                instance = new MultiCheckBoxRenderer();
            }
            return instance;
        }


        @Override
        public SafeHtml render(MultiCheckBoxState object) {
            if (object.showCheckAll()) {
                if (object.checkedCount() < object.totalCount()) {
                    return template.standard(object.checkedCount(), object.totalCount());
                }
                return template.allSelected(object.totalCount());
            }
            return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
    }

    public static class CheckBoxFooter extends Header<MultiCheckBoxState> {

        private final MultiCheckBoxState checkBoxState;

        public CheckBoxFooter(MultiCheckBoxState checkBoxState) {
            super(new MultiCheckBoxCell());
            this.checkBoxState = checkBoxState;
        }

        @Override
        public MultiCheckBoxState getValue() {
            return checkBoxState;
        }
    }

    public static class CheckBoxHeader extends Header<Boolean> {

        protected boolean checked = false;

        public CheckBoxHeader() {
            super(new CheckboxCell(true, false));
        }

        @Override
        public Boolean getValue() {
            return checked;
        }

        public void reverseChecked() {
            checked = !checked;
        }
    }


    public class StudyColumn extends Column<CandidateGeneListEnrichmentProxy, StudyProxy> {
        public StudyColumn(PlaceManager placeManager, PlaceRequest.Builder request) {
            super(new StudyListDataGridColumns.TitleCell(request, placeManager));
        }

        @Override
        public StudyProxy getValue(CandidateGeneListEnrichmentProxy object) {
            return object.getStudy();
        }
    }

    public class TitleColumn extends Column<CandidateGeneListEnrichmentProxy, CandidateGeneListProxy> {

        public TitleColumn(PlaceManager placeManager, PlaceRequest.Builder request) {
            super(new TitleCell(request, placeManager));
        }

        @Override
        public CandidateGeneListProxy getValue(CandidateGeneListEnrichmentProxy object) {
            return object.getCandidateGeneList();
        }
    }

    public static class TitleCell extends AbstractCell<CandidateGeneListProxy> {

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
        public void render(Context context, CandidateGeneListProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            placeRequest.with("id", value.getId().toString());
            SafeUri link = UriUtils.fromTrustedString("#" + placeManager.buildHistoryToken(placeRequest.build()));
            SafeHtml name = SafeHtmlUtils.fromString(value.getName());
            String description = value.getDescription();
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            if (description != null) {
                builder.append(SafeHtmlUtils.fromString(description));
            }
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
