package com.gmi.nordborglab.browser.client.mvp.view.home;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.gmi.nordborglab.browser.client.mvp.handlers.BasicStudyWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.BasicStudyWizardPresenter;
import com.gmi.nordborglab.browser.client.resources.CardCellListResources;
import com.gmi.nordborglab.browser.client.ui.ExperimentCard;
import com.gmi.nordborglab.browser.client.ui.GenotypeCard;
import com.gmi.nordborglab.browser.client.ui.PhenotypeCard;
import com.gmi.nordborglab.browser.client.ui.WizardPanel;
import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import elemental.client.Browser;
import elemental.dom.DataTransferItem;
import elemental.dom.DataTransferItemList;
import elemental.html.File;
import elemental.html.FileList;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 1/28/13
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicStudyWizardView extends ViewWithUiHandlers<BasicStudyWizardUiHandlers> implements BasicStudyWizardPresenter.MyView {



    public interface Binder extends UiBinder<Widget,BasicStudyWizardView> {}



    private final Widget widget;
    private ExperimentCard selectedExperiment;
    private final PhenotypeCard phenotypeCardCell;

    @UiField
    WizardPanel wizard;
    @UiField
    FlowPanel experimentsContainer;

    @UiField
    Button createExperimentBtn;
    @UiField HTMLPanel selectExperimentPanel;
    @UiField
    Modal createExperimentPanel;
    @UiField
    InlineLabel phenotypeCount;
    @UiField LayoutPanel selectPhenotypePanel;

    @UiField SimpleLayoutPanel phenotypeUploadPanel;

    @UiField
    LayoutPanel phenotypeContainterPanel;

    @UiField
    TextBox experimentNameTb;
    @UiField
    TextBox experimentOriginatorTb;
    @UiField
    TextArea experimentDesignTb;

    @UiField
    com.github.gwtbootstrap.client.ui.TextBox phenotypeSearchBox;

    @UiField(provided=true)
    CellList<PhenotypeProxy> phenotypeList;
    @UiField(provided=true) CellList<AlleleAssayProxy> genotypeList;

    ClickHandler experimentClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            Object source = event.getSource();
            onSelectExperimentCard(source);
        }
    };

    KeyUpHandler experimentKeyUpHandler = new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                onSelectExperimentCard(event.getSource());
            }
        }
    };

    @Inject
    public BasicStudyWizardView(final Binder binder,
                                final PhenotypeCard phenotypeCard,final GenotypeCard genotypeCard,
                                final CardCellListResources cardCellListResources ) {
        this.phenotypeCardCell = phenotypeCard;
        phenotypeList = new CellList<PhenotypeProxy>(phenotypeCard,cardCellListResources, new EntityProxyKeyProvider<PhenotypeProxy>());
        genotypeList = new CellList<AlleleAssayProxy>(genotypeCard,cardCellListResources,new EntityProxyKeyProvider<AlleleAssayProxy>());
        widget = binder.createAndBindUi(this);
        wizard.addCancelButtonClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onCancel();
            }
        });
        wizard.addNextButtonClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onNext();
            }
        });
        wizard.addPreviousButtonClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onPrevious();

            }
        });
        phenotypeContainterPanel.setWidgetVisible(phenotypeUploadPanel,false);
    }


    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setNextStep() {
        wizard.nextStep();
    }

    @Override
    public void setPreviousStep() {
        wizard.previousStep();
    }

    @Override
    public SearchTerm getPhenotypeSearchTerm() {
        return phenotypeCardCell.getSearchTerm();
    }

    @Override
    public void setExperiments(List<ExperimentProxy> experiments) {
        selectedExperiment = null;
        experimentsContainer.clear();
        for (ExperimentProxy experiment: experiments) {
            ExperimentCard card = new ExperimentCard();
            card.setExperiment(experiment);
            card.addClickHandler(experimentClickHandler);
            card.addKeyUpHandler(experimentKeyUpHandler);
            experimentsContainer.add(card);
        }
    }


    @UiHandler("createExperimentBtn")
    public void onClickCreateExperiment(ClickEvent e) {
        getUiHandlers().onShowCreateExperimentPanel();
    }


    @UiHandler("phenotypeSearchBox")
    public void onKeyUpPhenotypeSearchBox(KeyUpEvent e) {
        getUiHandlers().onSearchPhenotypeName(phenotypeSearchBox.getText());
    }

    @UiHandler("selectPhenotypeBtn")
    public void onClickCreatePhenotype(ClickEvent e) {
        phenotypeContainterPanel.setWidgetVisible(selectPhenotypePanel,true);
        phenotypeContainterPanel.setWidgetVisible(phenotypeUploadPanel,false);
    }

    @UiHandler("uploadPhenotypeBtn")
    public void onClickUploadPhenotype(ClickEvent e) {
        phenotypeContainterPanel.setWidgetVisible(selectPhenotypePanel,false);
        phenotypeContainterPanel.setWidgetVisible(phenotypeUploadPanel,true);
    }


    @UiHandler("saveExperimentBtn")
    public void onClickSaveExperimentBtn(ClickEvent e) {
        getUiHandlers().onSaveExperiment(experimentNameTb.getText(),experimentOriginatorTb.getText(),experimentDesignTb.getText());
    }



    @Override
    public ExperimentProxy getSelectedExperiment() {
        if (!selectExperimentPanel.isVisible())
            return null;
        if (selectedExperiment == null)
            return null;
        return selectedExperiment.getExperiment();
    }




    @Override
    public void showCreateExperimentPanel(boolean show) {
        createExperimentPanel.show();
    }


    @Override
    public boolean isShowUploadPhenotypePanel() {
        return phenotypeUploadPanel.isVisible();
    }

    @Override
    public HasData<AlleleAssayProxy> getGenotypeListDisplay() {
        return genotypeList;
    }

    @Override
    public void setSelectedExperiment(ExperimentProxy experiment) {
        for (int i = 0;i<experimentsContainer.getWidgetCount();i++) {
            Widget widget =experimentsContainer.getWidget(i);
            if (widget instanceof ExperimentCard) {
                ExperimentCard card = (ExperimentCard)widget;
                if (card.getExperiment() == experiment) {
                    selectedExperiment = card;
                    card.setSelected(true);
                    return;
                }
            }
        }
    }

    @Override
    public void hideCreateExperimentPopup() {
        createExperimentPanel.hide();
    }

    @Override
    public HasData<PhenotypeProxy> getPhenotypeListDisplay() {
        return phenotypeList;
    }

    @Override
    public void setPhenotypeCount(int totalCount, int visibleCount) {
        phenotypeCount.setText(visibleCount + " / " + totalCount);
    }


    private void onSelectExperimentCard(Object source) {
        if (source instanceof FocusPanel) {
            ExperimentCard card = (ExperimentCard) ((FocusPanel) source).getParent();
            if (selectedExperiment != null && selectedExperiment != card) {
                selectedExperiment.setSelected(false);
                getUiHandlers().onSelectedExperimentChanged();
            }
            selectedExperiment = card;
            selectedExperiment.setSelected(true);
        }
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == BasicStudyWizardPresenter.TYPE_SetPhenotypeUploadContent) {
            phenotypeUploadPanel.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }
}