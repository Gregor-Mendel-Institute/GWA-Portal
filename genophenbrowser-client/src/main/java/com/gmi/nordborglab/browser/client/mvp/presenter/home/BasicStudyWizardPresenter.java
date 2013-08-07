package com.gmi.nordborglab.browser.client.mvp.presenter.home;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PhenotypeUploadedEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.manager.HelperManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.BasicStudyWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.client.util.Statistics;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.service.CdvRequest;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.gmi.nordborglab.browser.shared.util.*;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
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
public class BasicStudyWizardPresenter extends Presenter<BasicStudyWizardPresenter.MyView, BasicStudyWizardPresenter.MyProxy> implements BasicStudyWizardUiHandlers {

    @ProxyCodeSplit
    @NameToken(NameTokens.basicstudywizard)
    @UseGatekeeper(IsLoggedInGatekeeper.class)
    public interface MyProxy extends ProxyPlace<BasicStudyWizardPresenter> {

    }

    public interface MyView extends View, HasUiHandlers<BasicStudyWizardUiHandlers> {

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

        void onShowPhenotypeUploadPanel(boolean isSHow);

        void showTransformationHistogram(TransformationDataProxy.TYPE type, ImmutableSortedMap<Double, Integer> histogram, Double shapiroPval);

        void setAvailableTransformations(List<TransformationProxy> transformationList);

        TransformationProxy getSelectedTransformation();

        void setMethods(List<StudyProtocolProxy> methods);

        StudyProtocolProxy getSelectedMethod();

        HasText getStudyText();

        void validateInputs();

        void updateSummaryView(AlleleAssayProxy alleleAssayProxy, PhenotypeProxy phenotype);

        void setStatisticTypes(List<StatisticTypeProxy> statisticTypes, List<Long> statisticTypesTraitCounts);

        void setPhenotypeHistogramData(ImmutableSortedMap<Double, Integer> histogram);

        void setPhenotypExplorerData(ImmutableList<TraitProxy> traitValues);

        void setGeoChartData(ImmutableMultiset geoChartData);

        void showPhenotypeCharts();

        void resetView();

        void setAvailableStatisticTypes(List<StatisticTypeProxy> statisticTypes);

        StatisticTypeProxy getSelectedStatisticType();

        void setAvailableAlleleAssays(List<AlleleAssayProxy> alleleAssayList);

        HasData<TraitProxy> getMissingGenotypeDisplay();

        HasValue<Boolean> getIsStudyJob();

        void showCallout(String callout, boolean show);
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
            return (query == null || query.length() == 0 || input == null || input.getId() == null || input.getLocalTraitName().indexOf(query) >= 0);
        }

    }

    @ContentSlot
    public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetPhenotypeUploadContent = new GwtEvent.Type<RevealContentHandler<?>>();
    private final PlaceManager placeManager;
    private ImmutableSortedMap<Double, Integer> histogram;
    private final ExperimentManager experimentManager;
    private final PhenotypeManager phenotypeManager;
    private final ListDataProvider<PhenotypeProxy> phenotypeDataProvider = new ListDataProvider<PhenotypeProxy>();
    private final ListDataProvider<AlleleAssayProxy> genotypeDataProvider = new ListDataProvider<AlleleAssayProxy>();
    private final ListDataProvider<TraitProxy> missingGenotypeDataProvider = new ListDataProvider<TraitProxy>();

    private final SingleSelectionModel<PhenotypeProxy> phenotypeSelectionModel = new SingleSelectionModel<PhenotypeProxy>();
    private final SingleSelectionModel<AlleleAssayProxy> genotypeSelectionModel = new SingleSelectionModel<AlleleAssayProxy>();
    private final PhenotypeNamePredicate phenotypeNamePredicate = new PhenotypeNamePredicate("");
    protected List<ExperimentProxy> availableExperiments;
    private List<PhenotypeProxy> phenotypeList;
    private final CurrentUser currentUser;
    private final PhenotypeUploadWizardPresenterWidget phenotypeUploadWizard;
    private ImmutableList<TraitProxy> phenotypeValues = null;
    private StudyProtocolProxy selectedStudyProtocol;
    private ImmutableList<TraitProxy> filteredPhenotypeValues = null;
    private static int BIN_COUNT = 20;
    private TransformationProxy selectedTransformation;
    private final CdvManager cdvManager;
    private final HelperManager helperManager;
    protected HashMap<StatisticTypeProxy, List<TraitProxy>> statisticPhenotypeValueCache = new HashMap<StatisticTypeProxy, List<TraitProxy>>();


    private static class WizardStateIterator implements ListIterator<STATE> {

        private final List<STATE> states;
        private int i = 0;

        private WizardStateIterator(final List<STATE> states) {
            this.states = states;
        }

        @Override
        public boolean hasNext() {
            return i < states.size();
        }

        @Override
        public STATE next() {
            i = i + 1;
            return states.get(i);
        }

        @Override
        public boolean hasPrevious() {
            return i > 0;
        }

        @Override
        public STATE previous() {
            i = i - 1;
            return states.get(i);
        }

        @Override
        public int nextIndex() {
            return i++;
        }

        @Override
        public int previousIndex() {
            return i--;
        }

        public void reset() {
            i = 0;
        }

        @Override
        public void remove() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void set(STATE state) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void add(STATE state) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    public enum STATE {
        EXPERIMENT, PHENOTYPE, GENOTYPE, TRANSFORMATION, STUDY, SUMMARY, FINISH;
    }

    WizardStateIterator stateIterator = new WizardStateIterator(Lists.newArrayList(STATE.values()));
    private STATE currentState = null;


    @Inject
    public BasicStudyWizardPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                                     final PlaceManager placeManager,
                                     final ExperimentManager experimentManager,
                                     final PhenotypeManager phenotypeManager,
                                     final CdvManager cdvManager,
                                     final CurrentUser currentUser,
                                     final HelperManager helperManager,
                                     final PhenotypeUploadWizardPresenterWidget phenotypeUploadWizard
    ) {
        super(eventBus, view, proxy);
        this.currentUser = currentUser;
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
        this.cdvManager = cdvManager;
        this.helperManager = helperManager;
        this.experimentManager = experimentManager;
        this.phenotypeManager = phenotypeManager;
        getView().getPhenotypeListDisplay().setSelectionModel(phenotypeSelectionModel);
        getView().getGenotypeListDisplay().setSelectionModel(genotypeSelectionModel);
        getView().setAvailableTransformations(currentUser.getAppData().getTransformationList());
        getView().setMethods(currentUser.getAppData().getStudyProtocolList());
        genotypeDataProvider.addDataDisplay(getView().getGenotypeListDisplay());
        missingGenotypeDataProvider.addDataDisplay(getView().getMissingGenotypeDisplay());
        this.phenotypeUploadWizard = phenotypeUploadWizard;
        phenotypeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                statisticPhenotypeValueCache.clear();
                PhenotypeProxy selectedObj = phenotypeSelectionModel.getSelectedObject();
                if (selectedObj == null)
                    return;
                if (selectedObj.getId() == null) {
                    phenotypeSelectionModel.setSelected(selectedObj, false);
                    getView().onShowPhenotypeUploadPanel(true);
                } else {
                    getView().setStatisticTypes(selectedObj.getStatisticTypes(), selectedObj.getStatisticTypeTraitCounts());
                }
                getView().showCallout("phenotype", false);
            }
        });
        genotypeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                showMissingGenotypeTraitValues();
                getView().showCallout("genotype", false);
            }
        });
        getView().setAvailableStatisticTypes(currentUser.getAppData().getStatisticTypeList());
        getView().setAvailableAlleleAssays(currentUser.getAppData().getAlleleAssayList());
    }


    private void showMissingGenotypeTraitValues() {
        final AlleleAssayProxy selectedAlleleAssay = genotypeSelectionModel.getSelectedObject();
        if (selectedAlleleAssay == null) {
            return;
        }
        List<TraitProxy> traits = statisticPhenotypeValueCache.get(getView().getSelectedStatisticType());
        ImmutableList missingGenotypeTraitValues = ImmutableList.copyOf(Collections2
                .filter(
                        traits,
                        new Predicate<TraitProxy>() {

                            @Override
                            public boolean apply(@Nullable TraitProxy input) {
                                if (input == null)
                                    return true;
                                boolean isNotFound = true;
                                for (AlleleAssayProxy alleleAssay : input.getObsUnit().getStock().getPassport().getAlleleAssays()) {
                                    if (alleleAssay.getId() == selectedAlleleAssay.getId()) {
                                        isNotFound = false;
                                        break;
                                    }
                                }
                                return isNotFound;
                            }
                        }
                ));
        missingGenotypeDataProvider.setList(missingGenotypeTraitValues);
    }

    private void filterAndShowTransformations() {
        final AlleleAssayProxy selectedAlleleAssay = genotypeSelectionModel.getSelectedObject();
        List<TraitProxy> traits = statisticPhenotypeValueCache.get(getView().getSelectedStatisticType());
        filteredPhenotypeValues = ImmutableList.copyOf(Collections2
                .filter(
                        traits,
                        new Predicate<TraitProxy>() {

                            @Override
                            public boolean apply(@Nullable TraitProxy input) {
                                if (input == null)
                                    return false;
                                boolean isFound = false;
                                for (AlleleAssayProxy alleleAssay : input.getObsUnit().getStock().getPassport().getAlleleAssays()) {
                                    if (alleleAssay.getId() == selectedAlleleAssay.getId()) {
                                        isFound = true;
                                        break;
                                    }
                                }
                                return isFound;
                            }
                        }
                ));

        List<Double> traitValues = Lists.transform(filteredPhenotypeValues, Statistics.traitToDouble);
        histogram = PhenotypeHistogram.getHistogram(traitValues, BIN_COUNT);
        getView().showTransformationHistogram(TransformationDataProxy.TYPE.RAW, histogram, 0.0);
        fireEvent(new LoadingIndicatorEvent(true));
        helperManager.calculateTransformations(new Receiver<List<TransformationDataProxy>>() {
            @Override
            public void onSuccess(List<TransformationDataProxy> response) {
                for (TransformationDataProxy transformationData : response) {
                    fireEvent(new LoadingIndicatorEvent(false));
                    getView().showTransformationHistogram(transformationData.getType(), PhenotypeHistogram.getHistogram(transformationData.getValues(), BIN_COUNT), transformationData.getShapiroPval());
                }

            }
        }, traitValues);
    }


    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetMainContent, this);
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
        super.onReset();
        placeManager.setOnLeaveConfirmation("Do you really want to leave?");
        resetState();
        stateIterator.reset();
        currentState = STATE.EXPERIMENT;
        if (availableExperiments == null) {
            fireEvent(new LoadingIndicatorEvent(true));
            experimentManager.findAllWithAccess(new Receiver<List<ExperimentProxy>>(

            ) {
                @Override
                public void onSuccess(List<ExperimentProxy> response) {
                    availableExperiments = response;
                    fireEvent(new LoadingIndicatorEvent(false));
                    getView().setExperiments(availableExperiments);
                }
            }, AccessControlEntryProxy.EDIT);
        } else {
            getView().setExperiments(availableExperiments);
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        resetState();
        placeManager.setOnLeaveConfirmation(null);
    }

    private void resetState() {
        availableExperiments = null;
        phenotypeList = null;
        if (phenotypeSelectionModel.getSelectedObject() != null) {
            phenotypeSelectionModel.setSelected(phenotypeSelectionModel.getSelectedObject(), false);
        }
        phenotypeDataProvider.setList(new ArrayList<PhenotypeProxy>());
        selectedStudyProtocol = null;
        selectedTransformation = null;
        phenotypeNamePredicate.setQuery("");
        if (genotypeSelectionModel.getSelectedObject() != null) {
            genotypeSelectionModel.setSelected(genotypeSelectionModel.getSelectedObject(), false);
        }
        missingGenotypeDataProvider.setList(new ArrayList<TraitProxy>());
        getView().resetView();
    }

    @Override
    public void onNext() {
        boolean isOk = true;
        switch (currentState) {
            case EXPERIMENT:
                if (getView().getSelectedExperiment() == null) {
                    isOk = false;
                    getView().showCallout("experiments", true);
                    //showError("You must select an experiment or create a new one");
                    break;
                }
                if (!phenotypeDataProvider.getDataDisplays().contains(getView().getPhenotypeListDisplay())) {
                    phenotypeDataProvider.addDataDisplay(getView().getPhenotypeListDisplay());
                }
                loadPhenotypesForExperiment(null);
                phenotypeUploadWizard.setExperiment(getView().getSelectedExperiment());
                break;
            case PHENOTYPE:
                if (getView().isShowUploadPhenotypePanel()) {
                    boolean checkUploadOk = phenotypeUploadWizard.checkUploadOk();
                    if (!checkUploadOk) {
                        return;
                    }
                    phenotypeUploadWizard.save();
                    return;
                } else {
                    if (phenotypeSelectionModel.getSelectedObject() == null) {
                        isOk = false;
                        getView().showCallout("phenotype", true);
                        break;
                    }
                    if (getView().getSelectedStatisticType() == null) {
                        isOk = false;
                        getView().showCallout("statistictype", true);
                        break;
                    }
                    //TODO better way to remove the null values
                    cdvManager.findAlleleAssaysWithStats(new Receiver<List<AlleleAssayProxy>>() {
                        @Override
                        public void onSuccess(List<AlleleAssayProxy> response) {
                            genotypeDataProvider.setList(response);
                        }
                    }, phenotypeSelectionModel.getSelectedObject().getId(), getView().getSelectedStatisticType().getId());

                }
                break;
            case GENOTYPE:
                if (genotypeSelectionModel.getSelectedObject() == null) {
                    isOk = false;
                    getView().showCallout("genotype", true);
                    break;
                }
                filterAndShowTransformations();
                break;

            case TRANSFORMATION:
                selectedTransformation = getView().getSelectedTransformation();
                if (selectedTransformation == null) {
                    isOk = false;
                    showError("You must select a transformation");
                    break;
                }
                break;
            case STUDY:
                selectedStudyProtocol = getView().getSelectedMethod();
                if (selectedStudyProtocol == null) {
                    isOk = false;
                    getView().showCallout("method", true);
                    break;
                }
                getView().validateInputs();
                if (getView().getStudyText().getText().equals("")) {
                    isOk = false;
                    getView().showCallout("analysis", true);
                    break;
                }
                if (isOk)
                    getView().updateSummaryView(genotypeSelectionModel.getSelectedObject(), phenotypeSelectionModel.getSelectedObject());
                break;
            case SUMMARY:
                CdvRequest ctx = cdvManager.getContext();
                StudyProxy study = ctx.create(StudyProxy.class);
                study.setAlleleAssay(genotypeSelectionModel.getSelectedObject());
                study.setName(getView().getStudyText().getText());
                study.setProtocol(selectedStudyProtocol);
                study.setTraits(ImmutableSet.copyOf(filteredPhenotypeValues));
                study.setTransformation(selectedTransformation);
                if (getView().getIsStudyJob().getValue()) {
                    StudyJobProxy studyJob = ctx.create(StudyJobProxy.class);
                    studyJob.setStatus("Waiting");
                    studyJob.setProgress(1);
                    studyJob.setCreateDate(new Date());
                    studyJob.setModificationDate(new Date());
                    studyJob.setTask("Waiting for workflow to start");
                    study.setJob(studyJob);
                }
                fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
                ctx.saveStudy(study).fire(new Receiver<StudyProxy>() {
                    @Override
                    public void onSuccess(StudyProxy response) {
                        fireEvent(new LoadingIndicatorEvent(false));
                        PlaceRequest placeRequest = new ParameterizedPlaceRequest(NameTokens.study).with("id", response.getId().toString());
                        placeManager.setOnLeaveConfirmation(null);
                        placeManager.revealPlace(placeRequest);
                        resetState();
                    }

                    @Override
                    public void onFailure(ServerFailure error) {
                        fireEvent(new LoadingIndicatorEvent(false));
                        fireEvent(new DisplayNotificationEvent("Error", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, DisplayNotificationEvent.DURATION_NORMAL));
                    }
                });
                break;
        }

        if (isOk) {
            currentState = stateIterator.next();
            getView().setNextStep();
        }
    }

    private void loadPhenotypesForExperiment(final Long phenotypeId) {
        fireEvent(new LoadingIndicatorEvent(true));
        phenotypeManager.findAllByAcl(new Receiver<List<PhenotypeProxy>>() {
            @Override
            public void onSuccess(List<PhenotypeProxy> response) {
                phenotypeList = response;
                phenotypeList.add(0, phenotypeManager.getContext().create(PhenotypeProxy.class));
                filterAndDisplayPhenotypeList(phenotypeId);
                getView().showPhenotypeCharts();
                fireEvent(new LoadingIndicatorEvent(false));
                /*if (response.size() == 0) {
                    getView().onShowPhenotypeUploadPanel(true);
                }
                else {
                    getView().onShowPhenotypeUploadPanel(false);
                } */
            }
        }, getView().getSelectedExperiment().getId(), AccessControlEntryProxy.EDIT);
    }

    private void filterAndDisplayPhenotypeList(final Long phenotypeId) {
        if (phenotypeList == null || phenotypeList.size() == 0)
            return;
        List<PhenotypeProxy> filteredPhenotypeValues = ImmutableList.copyOf(Collections2.filter(phenotypeList, phenotypeNamePredicate));
        getView().setPhenotypeCount(phenotypeList.size(), filteredPhenotypeValues.size());
        getView().getPhenotypeSearchTerm().setValue(phenotypeNamePredicate.getQuery());
        phenotypeDataProvider.setList(filteredPhenotypeValues);
        if (phenotypeId != null) {
            PhenotypeProxy proxy = Iterables.find(phenotypeList, new Predicate<PhenotypeProxy>() {
                @Override
                public boolean apply(@Nullable PhenotypeProxy input) {
                    if (input == null && input.getId() != null)
                        return false;
                    return input.getId().equals(phenotypeId);
                }
            }, null);
            if (proxy != null)
                phenotypeSelectionModel.setSelected(proxy, true);
        }
    }

    private void showError(String message) {
        DisplayNotificationEvent.fireError(this, "Error", message);
    }

    @Override
    public void onPrevious() {
        currentState = stateIterator.previous();
        if (currentState == STATE.EXPERIMENT) {

        }
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
        filterAndDisplayPhenotypeList(null);
    }

    @Override
    public void onSelectedExperimentChanged() {
        phenotypeList = null;
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(TYPE_SetPhenotypeUploadContent, phenotypeUploadWizard);
        registerHandler(PhenotypeUploadedEvent.register(getEventBus(), new PhenotypeUploadedEvent.Handler() {
            @Override
            public void onPhenotypeUploaded(PhenotypeUploadedEvent event) {
                if (isVisible() && currentState == STATE.PHENOTYPE) {
                    loadPhenotypesForExperiment(event.getPhentoypeId());
                    getView().onShowPhenotypeUploadPanel(false);
                }
            }
        }));
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
                DisplayNotificationEvent.fireError(BasicStudyWizardPresenter.this, "Error", "Failed to save experiment");
            }
        });
        ctx.fire();
    }

    @Override
    public void onSelectStatisticType(final StatisticTypeProxy statisticType) {
        if (phenotypeSelectionModel.getSelectedObject() == null || statisticType == null)
            return;

        List<TraitProxy> cachedTraits = statisticPhenotypeValueCache.get(statisticType);
        if (cachedTraits != null) {
            displayTraitValues(cachedTraits);
        } else {
            fireEvent(new LoadingIndicatorEvent(true));
            phenotypeManager.findAllTraitValuesByType(phenotypeSelectionModel.getSelectedObject().getId(), statisticType.getId(), new Receiver<List<TraitProxy>>() {

                @Override
                public void onSuccess(List<TraitProxy> response) {
                    fireEvent(new LoadingIndicatorEvent(false));
                    statisticPhenotypeValueCache.put(statisticType, response);
                    displayTraitValues(response);
                }
            });
        }

    }


    private void displayTraitValues(List<TraitProxy> traitValues) {
        getView().setPhenotypeHistogramData(PhenotypeHistogram.getHistogram(Lists.transform(traitValues, Statistics.traitToDouble), BIN_COUNT));
        getView().setPhenotypExplorerData(ImmutableList.copyOf(traitValues));
        getView().setGeoChartData(Statistics.getGeoChartData(traitValues));
        getView().showPhenotypeCharts();
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
