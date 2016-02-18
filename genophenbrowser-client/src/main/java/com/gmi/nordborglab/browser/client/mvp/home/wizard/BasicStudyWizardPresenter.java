package com.gmi.nordborglab.browser.client.mvp.home.wizard;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PhenotypeUploadedEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.manager.HelperManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.ApplicationPresenter;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.security.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.client.util.Statistics;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.GWASRuntimeInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.TransformationDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.TransformationProxy;
import com.gmi.nordborglab.browser.shared.service.CdvRequest;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.gmi.nordborglab.browser.shared.util.Normality;
import com.gmi.nordborglab.browser.shared.util.PhenotypeHistogram;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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

        BasicStudyWizardView.ExperimentEditDriver getExperimentDriver();

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

        void setStepNumber(int stepNumber);

        HasValue<Boolean> getIsCreateEnrichments();

        void updateGWASRuntime(GWASRuntimeInfoProxy info, int size);
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

    static final PermanentSlot<PhenotypeUploadWizardPresenterWidget> SLOT_PHENOTYPE_UPLOAD = new PermanentSlot<>();
    static final PermanentSlot<PhenotypeUploadWizardPresenterWidget> SLOT_ISATAB_UPLOAD = new PermanentSlot<>();

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
    private final PhenotypeUploadWizardPresenterWidget isaTabUploadWizard;
    private ImmutableList<TraitProxy> phenotypeValues = null;
    private StudyProtocolProxy selectedStudyProtocol;
    private ImmutableList<TraitProxy> filteredPhenotypeValues = null;
    private static int BIN_COUNT = 20;
    private TransformationProxy selectedTransformation;
    private final CdvManager cdvManager;
    private final HelperManager helperManager;
    protected HashMap<StatisticTypeProxy, List<TraitProxy>> statisticPhenotypeValueCache = new HashMap<StatisticTypeProxy, List<TraitProxy>>();
    private Long experimentId = null;
    private Long phenotypeId = null;
    private ExperimentProxy newExperiment;
    private final Validator validator;
    private final GoogleAnalyticsManager analyticsManager;


    private static class WizardStateIterator implements ListIterator<STATE> {

        private final List<STATE> states;
        private int i = 0;

        private WizardStateIterator(final List<STATE> states) {
            this.states = states;
        }

        @Override
        public boolean hasNext() {
            return (i+1) < states.size();
        }

        @Override
        public STATE next() {
            if (hasNext()) {
                i = i + 1;
            }
            return states.get(i);
        }

        @Override
        public boolean hasPrevious() {
            return i > 0;
        }

        @Override
        public STATE previous() {
            if (hasPrevious()) {
                i = i - 1;
            }
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
                                     final PhenotypeUploadWizardPresenterWidget phenotypeUploadWizard,
                                     final PhenotypeUploadWizardPresenterWidget isaTabUploadWizard,
                                     Validator validator,
                                     final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_MAIN_CONTENT);
        this.currentUser = currentUser;
        this.analyticsManager = analyticsManager;
        this.validator = validator;
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
        phenotypeDataProvider.addDataDisplay(getView().getPhenotypeListDisplay());
        missingGenotypeDataProvider.addDataDisplay(getView().getMissingGenotypeDisplay());
        this.phenotypeUploadWizard = phenotypeUploadWizard;
        this.isaTabUploadWizard = isaTabUploadWizard;
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
                                    if (alleleAssay.getId().equals(selectedAlleleAssay.getId())) {
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
                                    if (alleleAssay.getId().equals(selectedAlleleAssay.getId())) {
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
        double shapiroPval = Normality.getShapiroWilkPvalue(traitValues);
        if (shapiroPval > 0.0) {
            shapiroPval = Math.round(-Math.log10(shapiroPval) * 100.0) / 100.0;
        }
        getView().showTransformationHistogram(TransformationDataProxy.TYPE.RAW, histogram, shapiroPval);
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
    public void onCancel() {
        resetState();
        PlaceRequest request = new PlaceRequest.Builder().nameToken(
                NameTokens.home).build();
        placeManager.revealPlace(request);
    }

    @Override
    public void onReset() {
        super.onReset();
        placeManager.setOnLeaveConfirmation("Do you really want to leave?");
        resetState();
        stateIterator.reset();
        currentState = STATE.EXPERIMENT;
        PlaceRequest currentRequest = placeManager.getCurrentPlaceRequest();
        if (currentRequest.getParameterNames().contains("phenotype")) {
            try {
                phenotypeId = Long.parseLong(currentRequest.getParameter("phenotype", null));
            } catch (Exception e) {
            }
        }


        if (availableExperiments == null) {
            fireEvent(new LoadingIndicatorEvent(true));
            experimentManager.findAllWithAccess(new Receiver<List<ExperimentProxy>>(

            ) {
                @Override
                public void onSuccess(List<ExperimentProxy> response) {
                    availableExperiments = response;
                    fireEvent(new LoadingIndicatorEvent(false));
                    getView().setExperiments(availableExperiments);
                    loadAndSelectPhenotype(phenotypeId);
                }
            }, AccessControlEntryProxy.EDIT);
        } else {
            getView().setExperiments(availableExperiments);
            loadAndSelectPhenotype(phenotypeId);
        }
    }

    private void loadAndSelectPhenotype(final Long phenotypeId) {
        if (phenotypeId == null)
            return;
        phenotypeManager.getContext().findPhenotype(phenotypeId).with("experiment").fire(new Receiver<PhenotypeProxy>() {
            @Override
            public void onSuccess(PhenotypeProxy response) {
                selectExperiment(response.getExperiment().getId());
                loadPhenotypesForExperiment(phenotypeId);
                currentState = stateIterator.next();
                getView().setStepNumber(1);
            }
        });

    }

    private void selectExperiment(final Long experimentId) {
        if (experimentId == null || availableExperiments == null)
            return;
        ExperimentProxy experiment = Iterables.find(availableExperiments, new Predicate<ExperimentProxy>() {
            @Override
            public boolean apply(@Nullable ExperimentProxy experimentProxy) {
                if (experimentProxy != null && experimentId.equals(experimentProxy.getId())) {
                    return true;
                }
                return false;
            }
        });
        getView().setSelectedExperiment(experiment);
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
        phenotypeId = null;
        experimentId = null;
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
                calculateAndUpdateRuntime();
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
                    break;
                }
                if (isOk)
                    getView().updateSummaryView(genotypeSelectionModel.getSelectedObject(), phenotypeSelectionModel.getSelectedObject());
                break;
            case SUMMARY:
                CdvRequest ctx = cdvManager.getContext();
                final StudyProxy study = ctx.create(StudyProxy.class);
                study.setAlleleAssay(genotypeSelectionModel.getSelectedObject());
                study.setName(getView().getStudyText().getText());
                study.setProtocol(selectedStudyProtocol);
                study.setTraits(ImmutableSet.copyOf(filteredPhenotypeValues));
                study.setTransformation(selectedTransformation);
                study.setCreateEnrichments(getView().getIsCreateEnrichments().getValue());
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
                analyticsManager.startTimingEvent("BasicStudyWizard", "Create");
                ctx.saveStudy(study).fire(new Receiver<StudyProxy>() {
                    @Override
                    public void onSuccess(StudyProxy response) {
                        fireEvent(new LoadingIndicatorEvent(false));
                        analyticsManager.endTimingEvent("BasicStudyWizard", "Create", "OK");
                        analyticsManager.sendEvent("BasicStudyWizard", "Create", "Study:" + response.getId().toString());
                        PlaceRequest placeRequest = new PlaceRequest.Builder()
                                .nameToken(NameTokens.study)
                                .with("id", response.getId().toString()).build();
                        placeManager.setOnLeaveConfirmation(null);
                        placeManager.revealPlace(placeRequest);
                        resetState();

                    }

                    @Override
                    public void onFailure(ServerFailure error) {
                        fireEvent(new LoadingIndicatorEvent(false));
                        analyticsManager.endTimingEvent("BasicStudyWizard", "Create", "ERROR");
                        analyticsManager.sendError("BasicStudyWizard", error.getMessage(), true);
                        DisplayNotificationEvent.fireError(getEventBus(), "Error", error.getMessage());
                    }
                });
                break;
        }

        if (isOk) {
            currentState = stateIterator.next();
            getView().setNextStep();
        }
    }

    private void calculateAndUpdateRuntime() {
        final AlleleAssayProxy selectedAlleleAssay = genotypeSelectionModel.getSelectedObject();
        if (selectedAlleleAssay == null)
            return;
        List<TraitProxy> traits = statisticPhenotypeValueCache.get(getView().getSelectedStatisticType());
        Set<GWASRuntimeInfoProxy> runtimeInfo = currentUser.getRuntimeInfoFromAlleleAssayId(selectedAlleleAssay.getId());
        for (GWASRuntimeInfoProxy info : runtimeInfo) {
            getView().updateGWASRuntime(info, traits.size());
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
            }
        }, getView().getSelectedExperiment().getId(), AccessControlEntryProxy.EDIT);
    }

    private void filterAndDisplayPhenotypeList(final Long phenotypeId) {
        if (phenotypeList == null || phenotypeList.size() == 0)
            return;
        List<PhenotypeProxy> filteredPhenotypeValues = FluentIterable.from(phenotypeList).filter(phenotypeNamePredicate).toSortedList(new Comparator<PhenotypeProxy>() {
            @Override
            public int compare(PhenotypeProxy o1, PhenotypeProxy o2) {
                if (phenotypeId == null) {
                    return 0;
                }
                if (o1.getId() == null) {
                    return -1;
                }
                if (o2.getId() == null) {
                    return 1;
                }
                if (o1.getId().equals(phenotypeId)) {
                    return -1;
                } else if (o2.getId().equals(phenotypeId)) {
                    return 1;
                }
                return 0;
            }
        });
        getView().setPhenotypeCount(phenotypeList.size(), filteredPhenotypeValues.size());
        getView().getPhenotypeSearchTerm().setValue(phenotypeNamePredicate.getQuery());
        phenotypeDataProvider.setList(filteredPhenotypeValues);
        if (phenotypeId != null) {
            PhenotypeProxy proxy = Iterables.find(phenotypeList, new Predicate<PhenotypeProxy>() {
                @Override
                public boolean apply(@Nullable PhenotypeProxy input) {
                    if (input == null || input.getId() == null)
                        return false;
                    return input.getId().equals(phenotypeId);
                }
            }, null);
            if (proxy != null)
                phenotypeSelectionModel.setSelected(proxy, true);
        }
    }

    private void showError(String message) {
        DisplayNotificationEvent.fireError(getEventBus(), "Error", message);
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
        ExperimentRequest ctx = experimentManager.getContext();
        newExperiment = ctx.create(ExperimentProxy.class);
        ctx.save(newExperiment).to(new Receiver<ExperimentProxy>() {
            @Override
            public void onSuccess(ExperimentProxy response) {
                addExperimentToAvailableSetAndDisplay(response);
            }

            @Override
            public void onFailure(ServerFailure error) {
                DisplayNotificationEvent.fireError(getEventBus(), "Error", error.getMessage());
            }

            @Override
            public void onConstraintViolation(Set<ConstraintViolation<?>> violations) {
                getView().getExperimentDriver().setConstraintViolations(violations);
            }
        });
        getView().getExperimentDriver().edit(newExperiment, ctx);
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
        isaTabUploadWizard.setExperiment(null);
        setInSlot(SLOT_PHENOTYPE_UPLOAD, phenotypeUploadWizard);
        setInSlot(SLOT_ISATAB_UPLOAD, isaTabUploadWizard);
        registerHandler(getEventBus().addHandlerToSource(PhenotypeUploadedEvent.TYPE, phenotypeUploadWizard, new PhenotypeUploadedEvent.Handler() {
            @Override
            public void onPhenotypeUploaded(PhenotypeUploadedEvent event) {
                if (isVisible() && currentState == STATE.PHENOTYPE) {
                    loadPhenotypesForExperiment(null);
                    phenotypeUploadWizard.reset();
                    getView().onShowPhenotypeUploadPanel(false);
                }
            }
        }));

        registerHandler(getEventBus().addHandlerToSource(PhenotypeUploadedEvent.TYPE, isaTabUploadWizard, new PhenotypeUploadedEvent.Handler() {
            @Override
            public void onPhenotypeUploaded(PhenotypeUploadedEvent event) {
                addExperimentToAvailableSetAndDisplay(event.getExperiment());
                getView().hideCreateExperimentPopup();
            }
        }));
    }

    private boolean checkExperimentConstraint() {
        getView().getExperimentDriver().flush();
        if (newExperiment == null)
            return false;
        boolean isOk;
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
                .validate(newExperiment, Default.class);
        if (!violations.isEmpty() || getView().getExperimentDriver().hasErrors()) {
            isOk = false;
        } else {
            isOk = true;
        }
        getView().getExperimentDriver().setConstraintViolations(violations);
        return isOk;
    }

    @Override
    public void onSaveExperiment() {
        if (!checkExperimentConstraint())
            return;
        getView().getExperimentDriver().flush().fire();
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

    @Override
    public void onCloseCreateExperimentPopup() {
        isaTabUploadWizard.reset();
        newExperiment = null;
    }

    public void onCloseUploadPhentoypePopup() {
        phenotypeUploadWizard.reset();
    }


    private void displayTraitValues(List<TraitProxy> traitValues) {
        getView().setPhenotypeHistogramData(PhenotypeHistogram.getHistogram(Lists.transform(traitValues, Statistics.traitToDouble), BIN_COUNT));
        getView().setPhenotypExplorerData(ImmutableList.copyOf(traitValues));
        getView().setGeoChartData(Statistics.getGeoChartDataFromTrai(traitValues));
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
