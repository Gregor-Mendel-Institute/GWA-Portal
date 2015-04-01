package com.gmi.nordborglab.browser.client.mvp.profile;

import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.phenotypes.PhenotypeListDataGridColumns;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiments.ExperimentListDataGridColumns;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.studies.StudyListDataGridColumns;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.gwtbootstrap3.client.ui.RadioButton;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 30.10.13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class ProfileView extends ViewWithUiHandlers<ProfileUiHandlers> implements ProfilePresenter.MyView {

    private final Widget widget;
    @UiField
    ImageElement avatarImg;
    @UiField
    HeadingElement nameLb;
    @UiField
    ParagraphElement memberSinceLb;
    @UiField
    DivElement userTypeLb;
    @UiField
    SpanElement analysisCountLb;
    @UiField
    SpanElement phenotypeCountLb;
    @UiField
    SpanElement studyCountLb;
    @UiField
    SimpleLayoutPanel pagerContainer;
    @UiField
    RadioButton studyTypeBtn;
    @UiField
    RadioButton phenotypeBtn;
    @UiField
    RadioButton analysisBtn;
    @UiField
    org.gwtbootstrap3.client.ui.ButtonGroup typeBtnGroup;
    @UiField
    Element typeIcon;
    @UiField
    SpanElement typeLb;
    @UiField
    SimpleLayoutPanel dataGridContainer;
    @UiField
    AnchorElement editLink;

    private final DataGrid<ExperimentProxy> experimentDataGrid;
    private final CustomPager experimentPager = new CustomPager();
    private final DataGrid<PhenotypeProxy> phenotypeDataGrid;
    private final CustomPager phenotypePager = new CustomPager();
    private final DataGrid<StudyProxy> studyDataGrid;
    private final CustomPager studyPager = new CustomPager();
    private final PlaceManager placeManager;

    public interface Binder extends UiBinder<Widget, ProfileView> {

    }

    @Inject
    public ProfileView(final Binder binder, final CustomDataGridResources dataGridResources, final PlaceManager placeManager) {
        experimentDataGrid = new DataGrid<ExperimentProxy>(50, dataGridResources, new EntityProxyKeyProvider<ExperimentProxy>());
        experimentPager.setDisplay(experimentDataGrid);

        phenotypeDataGrid = new DataGrid<PhenotypeProxy>(50, dataGridResources, new EntityProxyKeyProvider<PhenotypeProxy>());
        phenotypePager.setDisplay(phenotypeDataGrid);

        studyDataGrid = new DataGrid<StudyProxy>(50, dataGridResources, new EntityProxyKeyProvider<StudyProxy>());
        studyPager.setDisplay(studyDataGrid);
        this.placeManager = placeManager;
        widget = binder.createAndBindUi(this);
        initExperimentDataGrid();
        initPhenotypeDataGrid();
        initStudyDataGrid();
    }


    private void initExperimentDataGrid() {
        experimentDataGrid.addColumn(new ExperimentListDataGridColumns.TitleColumn(placeManager, new PlaceRequest.Builder().nameToken(NameTokens.experiment)), "Name");
        experimentDataGrid.addColumn(new ExperimentListDataGridColumns.DesignColumn(), "Design");
        experimentDataGrid.addColumn(new AccessColumn(), "Access");
        experimentDataGrid.setColumnWidth(2, 150, Style.Unit.PX);
    }

    private void initPhenotypeDataGrid() {
        phenotypeDataGrid.setWidth("100%");
        phenotypeDataGrid.setEmptyTableWidget(new Label("No Records found"));

        phenotypeDataGrid.addColumn(new PhenotypeListDataGridColumns.TitleColumn(placeManager, new PlaceRequest.Builder().nameToken(NameTokens.phenotype)), "Name");

        phenotypeDataGrid.addColumn(new PhenotypeListDataGridColumns.ExperimentColumn(), "Experiment");
        phenotypeDataGrid.addColumn(
                new PhenotypeListDataGridColumns.TraitOntologyColumn(),
                "Trait-Ontology");
        phenotypeDataGrid.addColumn(new PhenotypeListDataGridColumns.EnvironOntologyColumn(), "Env-Ontology");
        phenotypeDataGrid.addColumn(new PhenotypeListDataGridColumns.ProtocolColumn(),
                "Protocol");
        phenotypeDataGrid.addColumn(new AccessColumn(), "Access");

        phenotypeDataGrid.setColumnWidth(0, 15, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(1, 15, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(2, 15, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(3, 15, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(4, 40, Style.Unit.PCT);
        phenotypeDataGrid.setColumnWidth(5, 100, Style.Unit.PX);
    }

    private void initStudyDataGrid() {
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.study);
        studyDataGrid.setWidth("100%");
        studyDataGrid.setEmptyTableWidget(new Label("No Records found"));
        studyDataGrid.addColumn(new StudyListDataGridColumns.TitleColumn(placeManager, request), "Name");
        studyDataGrid.addColumn(new StudyListDataGridColumns.ExperimentColumn(), "Study");
        studyDataGrid.addColumn(new StudyListDataGridColumns.PhenotypeColumn(), "Phenotype");
        studyDataGrid.addColumn(new StudyListDataGridColumns.AlleleAssayColumn(), "Genotype");
        studyDataGrid.addColumn(new StudyListDataGridColumns.ProtocolColumn(), "Protocol");
        studyDataGrid.addColumn(new StudyListDataGridColumns.TransformationColumn(), "Trans.");
        List<HasCell<StudyJobProxy, ?>> cells = Lists.newArrayList();
        cells.add(new StudyListDataGridColumns.StatusCell());
        cells.add(new StudyListDataGridColumns.ProgressCell());
        studyDataGrid.addColumn(new StudyListDataGridColumns.StatusColumn(cells), "Status");
        studyDataGrid.addColumn(new AccessColumn(), "Access");
        studyDataGrid.setColumnWidth(0, 25, Style.Unit.PCT);
        studyDataGrid.setColumnWidth(1, 25, Style.Unit.PCT);
        studyDataGrid.setColumnWidth(2, 25, Style.Unit.PCT);
        studyDataGrid.setColumnWidth(3, 25, Style.Unit.PCT);
        studyDataGrid.setColumnWidth(4, 80, Style.Unit.PX);
        studyDataGrid.setColumnWidth(5, 80, Style.Unit.PX);
        studyDataGrid.setColumnWidth(6, 200, Style.Unit.PX);
        studyDataGrid.setColumnWidth(7, 100, Style.Unit.PX);

    }

    @Override
    public HasData<ExperimentProxy> getExperimentDisplay() {
        return experimentDataGrid;
    }

    @Override
    public HasData<PhenotypeProxy> getPhenotypeDisplay() {
        return phenotypeDataGrid;
    }

    @Override
    public HasData<StudyProxy> getStudyDisplay() {
        return studyDataGrid;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setAvatarUrl(String url) {
        avatarImg.setSrc(url);
    }

    @Override
    public void setName(String name) {
        nameLb.setInnerText(name);
    }

    @Override
    public void setMemberSince(Date date) {
        String text = "member since ";
        if (date != null) {
            text = text + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(date);
        }
        memberSinceLb.setInnerText(text);
    }

    @Override
    public void setUserType(String type) {
        userTypeLb.setInnerText(type);
    }

    @Override
    public void displayStats(int numberOfStudies, int numberOfPhenotypes, int numberOfAnalysis) {
        studyCountLb.setInnerText(String.valueOf(numberOfStudies));
        phenotypeCountLb.setInnerText(String.valueOf(numberOfPhenotypes));
        analysisCountLb.setInnerText(String.valueOf(numberOfAnalysis));
    }

    @Override
    public void setActiveType(ProfilePresenter.TYPE type) {
        String label = "";
        studyTypeBtn.removeStyleName("active");
        analysisBtn.removeStyleName("active");
        phenotypeBtn.removeStyleName("active");
        DataGrid grid = null;
        CustomPager pager = null;
        String iconClass = "";
        switch (type) {
            case STUDY:
                label = "Studies";
                studyTypeBtn.addStyleName("active");
                grid = experimentDataGrid;
                pager = experimentPager;
                iconClass = "e_icon-thermometer";
                break;
            case PHENOTYPE:
                label = "Phenotypes";
                phenotypeBtn.addStyleName("active");
                grid = phenotypeDataGrid;
                pager = phenotypePager;
                iconClass = "e_icon-feather";
                break;
            case ANALYSIS:
                label = "Analyses";
                analysisBtn.addStyleName("active");
                grid = studyDataGrid;
                pager = studyPager;
                iconClass = "e_icon-monitor";
                break;
        }
        typeLb.setInnerText(label);
        typeIcon.setClassName(iconClass);
        if (dataGridContainer.getWidget() != grid) {
            dataGridContainer.clear();
            dataGridContainer.add(grid);
        }
        if (pagerContainer.getWidget() != pager) {
            pagerContainer.clear();
            pagerContainer.add(pager);
        }
    }

    @Override
    public void setEditUrl(String editUrl) {
        if (editUrl == null) {
            editLink.setHref("javascript:;");
            editLink.getStyle().setDisplay(Style.Display.NONE);
        } else {
            editLink.setHref(editUrl);
            editLink.getStyle().setDisplay(Style.Display.INLINE);
        }
    }

    @UiHandler("studyTypeBtn")
    public void onClickStudyBtn(ClickEvent e) {
        getUiHandlers().onChangeType(ProfilePresenter.TYPE.STUDY);
    }

    @UiHandler("phenotypeBtn")
    public void onClickPhenotypeBtn(ClickEvent e) {
        getUiHandlers().onChangeType(ProfilePresenter.TYPE.PHENOTYPE);
    }

    @UiHandler("analysisBtn")
    public void onClickAnalysisBtn(ClickEvent e) {
        getUiHandlers().onChangeType(ProfilePresenter.TYPE.ANALYSIS);
    }

}