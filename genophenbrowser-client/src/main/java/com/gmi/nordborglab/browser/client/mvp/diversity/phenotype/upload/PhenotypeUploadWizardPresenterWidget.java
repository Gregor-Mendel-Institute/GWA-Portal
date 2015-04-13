package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload;

import com.gmi.nordborglab.browser.client.dto.MyFactory;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PhenotypeUploadedEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.util.AutoBeanCloneUtils;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampleDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadWizardPresenterWidget extends PresenterWidget<PhenotypeUploadWizardPresenterWidget.MyView> implements PhenotypeUploadWizardUiHandlers {

    private ExperimentProxy experiment;

    public interface MyView extends View, HasUiHandlers<PhenotypeUploadWizardUiHandlers> {

        void updateTableWidth(int numberOfPhenotypes);

        void setErrorCount(int totalCount, int count);

        void showOverViewPanel();

        void resetChartData();

        HasData<SampleDataProxy> getSampleDataDisplay();

        void addColumns(List<PhenotypeUploadDataProxy> valueColumns);

        HasData<SampleDataProxy> getSummaryDisplay();

        void showExperimentEditor(boolean show);

        PhenotypeUploadWizardView.ExperimentUploadDataEditDriver getDriver();

        void showPhenotypeDetailPanel(int index);

        HasData<PhenotypeUploadDataProxy> getPhenotypeUploadList();

        void showFileUploadPanel();

        void resetFileUploadPanel();

        void scheduledLayout();
    }

    protected ExperimentUploadDataProxy data = null;

    public static final ProvidesKey<PhenotypeUploadDataProxy> phenotypeUploadKey = new ProvidesKey<PhenotypeUploadDataProxy>() {

        private final EntityProxyKeyProvider<PhenotypeProxy> phenotypeKey = new EntityProxyKeyProvider<>();

        @Override
        public Object getKey(PhenotypeUploadDataProxy item) {
            return phenotypeKey.getKey(item.getTraitUom());
        }
    };

    protected final CurrentUser currentUser;
    protected final ListDataProvider<PhenotypeUploadDataProxy> phenotypeUploadDataProvider = new ListDataProvider<>();
    protected final ListDataProvider<SampleDataProxy> sampleDataProvider = new ListDataProvider<>();
    protected final ListDataProvider<SampleDataProxy> summaryDataProvider = new ListDataProvider<>();
    private final SingleSelectionModel<PhenotypeUploadDataProxy> phenotypeSelectionModel = new SingleSelectionModel<>(phenotypeUploadKey);
    protected final MyFactory appDataFactory;
    protected ExperimentManager experimentManager;
    protected ExperimentRequest ctx;
    private final Validator validator;
    private final GoogleAnalyticsManager analyticsManager;
    private AutoBean<ExperimentUploadDataProxy> receivedBean;
    private int currentPhenotypeIndex = -1;
    private Multiset<String> geoChartData;
    private List<SampleDataProxy> sampleData;
    private List<SampleDataProxy> explorerData;
    private Map<String, Double> histogramData;
    private boolean isTableUpdated = false;


    @Inject
    public PhenotypeUploadWizardPresenterWidget(EventBus eventBus, MyView view,
                                                final CurrentUser currentUser,
                                                final MyFactory appDataFactory,
                                                final ExperimentManager experimentManager,
                                                final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        getView().setUiHandlers(this);
        this.currentUser = currentUser;
        this.analyticsManager = analyticsManager;
        this.appDataFactory = appDataFactory;
        this.experimentManager = experimentManager;

        getView().getPhenotypeUploadList().setSelectionModel(phenotypeSelectionModel);
        phenotypeUploadDataProvider.addDataDisplay(getView().getPhenotypeUploadList());
        summaryDataProvider.addDataDisplay(getView().getSummaryDisplay());
        sampleDataProvider.addDataDisplay(getView().getSampleDataDisplay());
        phenotypeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                // get the changes and update the phenotype list
                //getView().getDriver().flush();
                PhenotypeUploadDataProxy selectedObj = phenotypeSelectionModel.getSelectedObject();
                if (selectedObj == null) {
                    currentPhenotypeIndex = -1;
                    // only showOverviewPanel when there is data otherwise it was deselected by reset() function
                    if (data != null) {
                        getView().showOverViewPanel();
                    }
                    return;
                }

                currentPhenotypeIndex = getIndexOfPhenotype(selectedObj);
                getView().showPhenotypeDetailPanel(currentPhenotypeIndex);
                getView().resetChartData();
                isTableUpdated = false;
                sampleDataProvider.setList(Collections.<SampleDataProxy>emptyList());
                getView().getSampleDataDisplay().setRowCount(0, false);

                initChartdata();
            }
        });
    }

    private int getIndexOfPhenotype(final PhenotypeUploadDataProxy phenotype) {
        return Iterables.indexOf(data.getPhenotypes(), new Predicate<PhenotypeUploadDataProxy>() {
            @Override
            public boolean apply(@Nullable PhenotypeUploadDataProxy input) {
                if (input == null || phenotype == null || input.getTraitUom() == null || phenotype.getTraitUom() == null)
                    return false;
                return input.getTraitUom().stableId().equals(phenotype.getTraitUom().stableId());
            }
        });
    }

    private void initChartdata() {
        List<SampleDataProxy> sampleData = Lists.newArrayList();
        // GeoChart Data
        ImmutableMultiset.Builder<String> geoMapBuilder = ImmutableMultiset.builder();
        List<SampleDataProxy> explorerData = new ArrayList<>();
        Map<String, Double> histogramData = new HashMap<>();

        for (SampleDataProxy sample : data.getSampleData()) {
            String value = sample.getValues().get(currentPhenotypeIndex);
            if (value == null || value.isEmpty())
                continue;
            sampleData.add(sample);
            if (!(sample.isIdKnown() && !sample.isParseError() && (sample.getParseMask() & (1 << currentPhenotypeIndex + 1)) == 0))
                continue;
            explorerData.add(sample);
            try {
                geoMapBuilder.add(sample.getCountry());
            } catch (NullPointerException e) {
            }
            histogramData.put(sample.getPassportId().toString(), Double.parseDouble(value));
        }
        ImmutableMultiset<String> geochartData = geoMapBuilder.build();
        this.histogramData = histogramData;
        this.geoChartData = geochartData;
        // MotionChart & Histogram Data
        this.sampleData = sampleData;
        this.explorerData = explorerData;
        getView().scheduledLayout();
    }


    @Override
    public void onUploadFinished(String responseText) {
        receivedBean = AutoBeanCodex.decode(appDataFactory, ExperimentUploadDataProxy.class, responseText);
        onEdit();
        fireEvent(new LoadingIndicatorEvent(false));
    }

    private void onEdit() {
        ctx = experimentManager.getContext();
        cloneUploadData();
        getView().updateTableWidth(data.getPhenotypes().size());
        showUploadData();
        getView().getDriver().edit(data, ctx);
        ///TODO Fix this better.
        List<String> paths = ImmutableList.<String>builder().addAll(Arrays.asList(getView().getDriver().getPaths())).build();
        if (isIsaTabUpload()) {
            analyticsManager.startTimingEvent("Phenotype", "ISATAB-Upload");
        } else {
            analyticsManager.startTimingEvent("Phenotype", "Upload");
        }
        ctx.saveExperimentUploadData(data).with(paths.toArray(new String[0])).to(new Receiver<ExperimentProxy>() {
            @Override
            public void onSuccess(ExperimentProxy response) {
                fireEvent(new LoadingIndicatorEvent(false));
                // fire from source so we can distinguish in BasicStudyWizardPresenter
                getEventBus().fireEventFromSource(new PhenotypeUploadedEvent(response), PhenotypeUploadWizardPresenterWidget.this);
                // isatab upload
                String var = isIsaTabUpload() ? "ISATAB-Upload" : "Upload";
                analyticsManager.sendEvent("Phenotype", var, "Experiment:" + response.getName());
                analyticsManager.endTimingEvent("Phenotype", var, "OK");
            }

            @Override
            public void onFailure(ServerFailure message) {
                fireEvent(new LoadingIndicatorEvent(false));
                DisplayNotificationEvent.fireError(getEventBus(), "Error", "Failed to save data");
                onEdit();
                String var = isIsaTabUpload() ? "ISATAB-Upload" : "Upload";
                analyticsManager.endTimingEvent("Phenotype", var, "ERROR");
                analyticsManager.sendError("Phenotype", message.getMessage(), true);
            }

            @Override
            public void onConstraintViolation(Set<ConstraintViolation<?>> violations) {
                fireEvent(new LoadingIndicatorEvent(false));
                // filter redudant constraint violations and update flag
                displayConstraintViolations(violations);
            }
        });
        checkValidation();
    }



    private void cloneUploadData() {
        data = AutoBeanCloneUtils.cloneExperimentUploadData(receivedBean, experiment, ctx, currentUser.getAppData().getUnitOfMeasureList());


        Function<PhenotypeUploadDataProxy, List<String>> getOntologies = new Function<PhenotypeUploadDataProxy, List<String>>() {
            @Nullable
            @Override
            public List<String> apply(@Nullable PhenotypeUploadDataProxy input) {
                return Lists.newArrayList(input.getTraitOntology(), input.getEnvironmentOntology());
            }
        };

        Set<String> ontologies = ImmutableSet.copyOf(FluentIterable
                .from(data.getPhenotypes())
                .transformAndConcat(getOntologies)
                .filter(Predicates.notNull()));
        if (ontologies == null || ontologies.isEmpty())
            return;
        experimentManager.getRequestFactory().ontologyRequest().findAllByAcc(ontologies).fire(new Receiver<Set<TermProxy>>() {
            @Override
            public void onSuccess(Set<TermProxy> response) {
                if (response == null)
                    return;
                Map<String, TermProxy> ontologyMap = FluentIterable.from(response).uniqueIndex(new Function<TermProxy, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable TermProxy input) {
                        return input.getAcc();
                    }
                });
                for (PhenotypeUploadDataProxy phenotype : data.getPhenotypes()) {
                    if (phenotype.getTraitOntology() != null && ontologyMap.containsKey(phenotype.getTraitOntology())) {
                        phenotype.getTraitUom().setTraitOntologyTerm(ontologyMap.get(phenotype.getTraitOntology()));
                    }
                    if (phenotype.getEnvironmentOntology() != null && ontologyMap.containsKey(phenotype.getTraitOntology())) {
                        phenotype.getTraitUom().setEnvironOntologyTerm(ontologyMap.get(phenotype.getTraitOntology()));
                    }
                }
                getView().getDriver().edit(data, ctx);
                phenotypeUploadDataProvider.setList(data.getPhenotypes());

            }
        });

    }


    private void showUploadData() {
        // show result panel
        getView().showOverViewPanel();
        getView().addColumns(data.getPhenotypes());
        getView().setErrorCount(data.getPhenotypes().size(), countPhenotypesWithErrors());
        phenotypeUploadDataProvider.setList(data.getPhenotypes());
        summaryDataProvider.setList(data.getSampleData());
    }

    private int countPhenotypesWithErrors() {
        return Iterables.size(Iterables.filter(data.getPhenotypes(), new Predicate<PhenotypeUploadDataProxy>() {
            @Override
            public boolean apply(@Nullable PhenotypeUploadDataProxy input) {
                return input.getErrorCount() > 0;
            }
        }));
    }


    @Override
    public void onUploadError(String responseText) {
        fireEvent(new LoadingIndicatorEvent(false));
        //DisplayNotificationEvent.fireError(this, "Error uploading phentoype file", responseText);
    }

    @Override
    public void onCancel() {
        reset();
    }

    @Override
    public void onCreate() {
        save();
    }

    @Override
    public void startUpload() {
        fireEvent(new LoadingIndicatorEvent(true));
    }


    @Override
    public void updatePhenotypeData() {
        checkPhenotypeValidations();
        phenotypeUploadDataProvider.refresh();
    }


    @Override
    public void deselectPhenotypeCard() {
        if (phenotypeSelectionModel.getSelectedObject() != null) {
            phenotypeSelectionModel.setSelected(phenotypeSelectionModel.getSelectedObject(), false);
        }
    }

    @Override
    public Collection<SampleDataProxy> getExplorerData() {
        return explorerData;
    }

    @Override
    public Multiset<String> getGeoChartdata() {
        return geoChartData;
    }

    @Override
    public Map<String, Double> getHistogramData() {
        return histogramData;
    }


    @Override
    public void updateTable() {
        if (!isTableUpdated)
            sampleDataProvider.setList(sampleData);
        isTableUpdated = true;
    }

    @Override
    protected void onBind() {
        super.onBind();    //To change body of overridden methods use File | Settings | File Templates.
        //getView().setUnitOfMeasureList(currentUser.getAppData().getUnitOfMeasureList());
    }

    //TODO return directly from server
    private UnitOfMeasureProxy getUnitOfMeasureFromName(String name) {
        if (name == null)
            return null;
        for (UnitOfMeasureProxy unitOfMeasure : currentUser.getAppData().getUnitOfMeasureList()) {

            if (unitOfMeasure != null && unitOfMeasure.getUnitType().equalsIgnoreCase(name)) {
                return unitOfMeasure;
            }
        }
        return null;
    }

    public void save() {
        getView().getDriver().flush();
        if (checkValidation()) {
            fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
            // fire event
            ctx.fire();
        }
    }

    public void reset() {
        data = null;
        ctx = null;
        receivedBean = null;
        deselectPhenotypeCard();
        phenotypeUploadDataProvider.setList(Lists.<PhenotypeUploadDataProxy>newArrayList());
        sampleDataProvider.setList(Lists.<SampleDataProxy>newArrayList());
        summaryDataProvider.setList(Lists.<SampleDataProxy>newArrayList());
        getView().resetFileUploadPanel();
        getView().showFileUploadPanel();
    }

    public void setExperiment(ExperimentProxy experiment) {
        this.experiment = experiment;
        getView().showExperimentEditor(experiment == null);
    }

    public boolean checkUploadOk() {
        return checkValidation();
    }

    private void checkPhenotypeValidations() {
        getView().getDriver().flush();
        PhenotypeUploadDataProxy phenotypeUpload = data.getPhenotypes().get(currentPhenotypeIndex);
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
                .validate(phenotypeUpload, Default.class);
        displayPhenotypeConstraintViolations(violations);
    }

    private void displayPhenotypeConstraintViolations(Set<ConstraintViolation<?>> violations) {
        getView().getDriver().setConstraintViolations(violations);
        PhenotypeUploadDataProxy phenotypeUpload = data.getPhenotypes().get(currentPhenotypeIndex);
        boolean hasErrors = (violations != null && !violations.isEmpty());
        phenotypeUpload.setConstraintViolation(hasErrors);
        phenotypeUploadDataProvider.refresh();
    }

    private boolean checkValidation() {
        boolean isOk;
        getView().getDriver().flush();

        //FIXME necessary because @Valid not yet supported
        // http://code.google.com/p/google-web-toolkit/issues/detail?id=8856
        // http://code.google.com/p/google-web-toolkit/issues/detail?id=7266
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
                .validate(data, Default.class);
        for (PhenotypeUploadDataProxy phenotype : data.getPhenotypes()) {
            violations.addAll((Set<ConstraintViolation<?>>) (Set) validator.validate(phenotype.getTraitUom(), Default.class));
        }
        violations.addAll((Set<ConstraintViolation<?>>) (Set) validator.validate(data.getExperiment(), Default.class));
        if (!violations.isEmpty()) {
            isOk = false;
        } else {
            isOk = true;
        }
        displayConstraintViolations(violations);
        return isOk;
    }

    private void displayConstraintViolations(Set<ConstraintViolation<?>> violations) {
        getView().getDriver().setConstraintViolations(violations);
        // reset cards
        for (PhenotypeUploadDataProxy phenotype : data.getPhenotypes()) {
            phenotype.setConstraintViolation(false);
        }
        if (violations != null && !violations.isEmpty()) {

            Iterable<Integer> phenotypesWithViolations = ImmutableSet.copyOf(Iterables.transform(Iterables.filter(violations, new Predicate<ConstraintViolation<?>>() {
                @Override
                public boolean apply(@Nullable ConstraintViolation<?> input) {
                    return (input.getLeafBean() instanceof PhenotypeProxy);
                }
            }), new Function<ConstraintViolation<?>, Integer>() {
                @Nullable
                @Override
                public Integer apply(@Nullable final ConstraintViolation<?> constraint) {
                    return Iterables.indexOf(data.getPhenotypes(), new Predicate<PhenotypeUploadDataProxy>() {
                        @Override
                        public boolean apply(@Nullable PhenotypeUploadDataProxy input) {
                            return input.getTraitUom() == constraint.getLeafBean();
                        }
                    });
                }
            }));
            // update the cards
            for (Integer ix : phenotypesWithViolations) {
                data.getPhenotypes().get(ix).setConstraintViolation(true);
            }
            phenotypeUploadDataProvider.refresh();
            // show the first Detail page that contains an error
            if (Iterables.size(phenotypesWithViolations) > 0) {
                phenotypeSelectionModel.setSelected(data.getPhenotypes().get(Iterables.getFirst(phenotypesWithViolations, -1)), true);
            } else {
                phenotypeSelectionModel.setSelected(phenotypeSelectionModel.getSelectedObject(), false);
            }
        }
    }

    public boolean isIsaTabUpload() {
        return experiment == null;
    }
}
