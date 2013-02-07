package com.gmi.nordborglab.browser.client.mvp.presenter.home;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.BasicStudyWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 1/28/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicStudyWizardPresenter extends Presenter<BasicStudyWizardPresenter.MyView,BasicStudyWizardPresenter.MyProxy> implements BasicStudyWizardUiHandlers{
    @ProxyCodeSplit
    @NameToken(NameTokens.basicstudywizard)
    @UseGatekeeper(IsLoggedInGatekeeper.class)
    public interface MyProxy extends ProxyPlace<BasicStudyWizardPresenter>{

    }
    public interface MyView extends View,HasUiHandlers<BasicStudyWizardUiHandlers> {

        void setNextStep();

        void setPreviousStep();

        void setExperiments(List<ExperimentProxy> experiments);

        ExperimentProxy getSelectedExperiment();

        void showCreateExperimentPanel(boolean show);

        HasData<PhenotypeProxy> getPhenotypeListDisplay();

        void setPhenotypeCount(int totalCount, int visibleCount);

        SearchTerm getPhenotypeSearchTerm();

        boolean isShowUploadPhenotypePanel();

        HasData<AlleleAssayProxy> getGenotypeListDisplay();

        void setSelectedExperiment(ExperimentProxy experiment);

        void hideCreateExperimentPopup();
    }

    static class PhenotypeNamePredicate implements Predicate<PhenotypeProxy> {
        private String query;

        public PhenotypeNamePredicate(String query) {
            this.query = query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        @Override
        public boolean apply(@Nullable PhenotypeProxy input) {
            return (query == null || query.length() == 0 || input.getLocalTraitName().indexOf(query) >= 0);
        }
    }

    @ContentSlot
    public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetPhenotypeUploadContent = new GwtEvent.Type<RevealContentHandler<?>>();

    private final PlaceManager placeManager;
    private final ExperimentManager experimentManager;
    private final PhenotypeManager phenotypeManager;
    private final ListDataProvider<PhenotypeProxy> phenotypeDataProvider = new ListDataProvider<PhenotypeProxy>();
    private final ListDataProvider<AlleleAssayProxy> genotypeDataProvider = new ListDataProvider<AlleleAssayProxy>();
    private final SingleSelectionModel<PhenotypeProxy> phenotypeSelectionModel = new SingleSelectionModel<PhenotypeProxy>();
    private final SingleSelectionModel<AlleleAssayProxy>genotypeSelectionModel = new SingleSelectionModel<AlleleAssayProxy>();
    private final PhenotypeNamePredicate phenotypeNamePredicate = new PhenotypeNamePredicate("");
    protected List<ExperimentProxy> availableExperiments;
    private List<PhenotypeProxy> phenotypeList;
    private final CurrentUser currentUser;
    private final PhenotypeUploadWizardPresenterWidget phenotypeUploadWizard;


    public enum STATE {
        EXPERIMENT, PHENOTYPE, GENOTYPE,TRANSFORMATION,STUDY,SUMMARY;
    }

    private STATE currentState = null;

    ListIterator<STATE> stateIterator = Lists.newArrayList(STATE.values()).listIterator();



    @Inject
    public BasicStudyWizardPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                     final PlaceManager placeManager,
                                     final ExperimentManager experimentManager,
                                     final PhenotypeManager phenotypeManager,
                                     final CurrentUser currentUser,
                                     final PhenotypeUploadWizardPresenterWidget phenotypeUploadWizard
    ) {
        super(eventBus, view, proxy);
        this.currentUser = currentUser;
        getView().setUiHandlers(this);
        this.placeManager=placeManager;
        this.experimentManager = experimentManager;
        this.phenotypeManager = phenotypeManager;
        currentState = stateIterator.next();
        getView().getPhenotypeListDisplay().setSelectionModel(phenotypeSelectionModel);
        getView().getGenotypeListDisplay().setSelectionModel(genotypeSelectionModel);
        genotypeDataProvider.addDataDisplay(getView().getGenotypeListDisplay());
        this.phenotypeUploadWizard = phenotypeUploadWizard;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetMainContent,this);
    }

    @Override
    public void onCancel() {
        resetState();
        PlaceRequest request = new ParameterizedPlaceRequest(
                NameTokens.home);
        placeManager.revealPlace(request);
    }

    @Override
    public void onReset() {
        if (availableExperiments == null) {
            experimentManager.findAllWithAccess(new Receiver<List<ExperimentProxy>>(

            ) {
                @Override
                public void onSuccess(List<ExperimentProxy> response) {
                    availableExperiments = response;
                    getView().setExperiments(availableExperiments);
                }
            }, AccessControlEntryProxy.WRITE);
        }
        else {
            getView().setExperiments(availableExperiments);
        }
    }

    private void resetState() {
        availableExperiments = null;
    }

    @Override
    public void onNext() {
        if (currentState == null)
            currentState = stateIterator.next();
        boolean isOk = true;
        switch (currentState) {
            case EXPERIMENT:
                if (getView().getSelectedExperiment() == null)
                {
                    isOk = false;
                    showError("You must select an experiment or create a new one");
                    break;
                }
                if (!phenotypeDataProvider.getDataDisplays().contains(getView().getPhenotypeListDisplay())) {
                    phenotypeDataProvider.addDataDisplay(getView().getPhenotypeListDisplay());
                }
                phenotypeManager.findAllByAcl(new Receiver<List<PhenotypeProxy>>() {
                    @Override
                    public void onSuccess(List<PhenotypeProxy> response) {
                        phenotypeList = response;
                        filterAndDisplayPhenotypeList();
                    }
                },getView().getSelectedExperiment().getId(),AccessControlEntryProxy.WRITE);
               /* getView().getStudyCreateDriver().flush();
                @SuppressWarnings("unchecked")
                Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
                        .validate(study, Default.class);
                if (!violations.isEmpty()) {
                    getView().getStudyCreateDriver().setConstraintViolations(
                            violations);
                } else {
                    getView().setNextStep();
                    if (phenotypeSelectionModel.getSelectedSet().size() == 0) {
                        getView().showBlankColumnChart();
                        getView().showBlankGeoChart();
                    }
                    if (phenotypeValues == null) {
                        getView().resetPhenotypeDataGrid();
                        phenotypeManager.findAllTraitValues(
                                new Receiver<List<TraitProxy>>() {

                                    @Override
                                    public void onSuccess(List<TraitProxy> response) {
                                        phenotypeValues = ImmutableList
                                                .copyOf(response);
                                        filterAndShowPhenotypeValues(null);
                                    }

                                }, phenotype.getId(), study.getAlleleAssay()
                                .getId(), null);
                    }

                }*/
                break;
            case PHENOTYPE:

                if (getView().isShowUploadPhenotypePanel()) {

                }
                else  {
                    if (phenotypeSelectionModel.getSelectedObject() == null) {
                        isOk = false;
                        showError("You must select a phenotype or upload a new one");
                        break;
                    }
                    //TODO better way to remove the null values
                    genotypeDataProvider.setList(currentUser.getAppData().getAlleleAssayList());

                }
                /*study.setTraits(phenotypeSelectionModel.getSelectedSet());
                createRequest.saveStudy(study).with("alleleAssay","protocol","userPermission").fire(new Receiver<StudyProxy>() {

                    @Override
                    public void onFailure(ServerFailure error) {
                        fireEvent(new DisplayNotificationEvent("Error",error.getMessage(),true,DisplayNotificationEvent.LEVEL_ERROR,DisplayNotificationEvent.DURATION_NORMAL));
                    }

                    @Override
                    public void onConstraintViolation(
                            Set<ConstraintViolation<?>> violations) {
                        super.onConstraintViolation(violations);
                        StringBuilder builder = new StringBuilder();
                        for (ConstraintViolation<?> violation:violations) {
                            builder.append(violation.getMessage());
                        }
                        fireEvent(new DisplayNotificationEvent("Warning",builder.toString(),true,DisplayNotificationEvent.LEVEL_WARNING,DisplayNotificationEvent.DURATION_NORMAL));
                    }

                    @Override
                    public void onSuccess(StudyProxy study) {
                        fireEvent(new LoadStudyEvent(study));
                        resetState();
                        placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.study).with("id", study.getId().toString()));

                    }

                });*/
                break;
        }

        if (isOk) {
            currentState = stateIterator.next();
            getView().setNextStep();
        }
    }

    private void filterAndDisplayPhenotypeList() {
        if (phenotypeList == null || phenotypeList.size() == 0)
            return;
        List<PhenotypeProxy> filteredPhenotypeValues = ImmutableList.copyOf(Collections2.filter(phenotypeList,phenotypeNamePredicate));
        getView().setPhenotypeCount(phenotypeList.size(),filteredPhenotypeValues.size());
        getView().getPhenotypeSearchTerm().setValue(phenotypeNamePredicate.getQuery());
        phenotypeDataProvider.setList(filteredPhenotypeValues);
    }

    private void showError(String message) {
        DisplayNotificationEvent.fireError(this,"Error",message);
    }

    @Override
    public void onPrevious() {
        currentState = stateIterator.previous();
        getView().setPreviousStep();
    }

    @Override
    public void onShowSelectExperimentPanel() {
        getView().showCreateExperimentPanel(false);
    }

    @Override
    public void onShowCreateExperimentPanel() {
        getView().showCreateExperimentPanel(true);
    }

    @Override
    public void onSearchPhenotypeName(String query) {
        phenotypeNamePredicate.setQuery(query);
        filterAndDisplayPhenotypeList();
    }

    @Override
    public void onSelectedExperimentChanged() {
        phenotypeList = null;
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(TYPE_SetPhenotypeUploadContent,phenotypeUploadWizard);
    }

    @Override
    public void onSaveExperiment(String name, String originator, String design) {
        ExperimentRequest ctx = experimentManager.getContext();
        ExperimentProxy experiment = ctx.create(ExperimentProxy.class);
        experiment.setName(name);
        experiment.setOriginator(originator);
        experiment.setDesign(design);
        ctx.save(experiment).to(new Receiver<ExperimentProxy>() {
            @Override
            public void onSuccess(ExperimentProxy response) {
                addExperimentToAvailableSetAndDisplay(response);
            }

            @Override
            public void onFailure(ServerFailure error) {
                DisplayNotificationEvent.fireError(BasicStudyWizardPresenter.this,"Error","Failed to save experiment");
            }
        });
        ctx.fire();
    }

    private void addExperimentToAvailableSetAndDisplay(ExperimentProxy experiment) {
        if (availableExperiments == null) 
            availableExperiments = new ArrayList<ExperimentProxy>();
        availableExperiments.add(experiment);
        getView().setExperiments(availableExperiments);
        getView().setSelectedExperiment(experiment);
        getView().hideCreateExperimentPopup();
    }
}
