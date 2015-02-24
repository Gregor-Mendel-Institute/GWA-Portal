package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload;

import com.gmi.nordborglab.browser.client.dto.MyFactory;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.GoogleAnalyticsEvent;
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
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.event.shared.EventBus;
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
import java.util.Arrays;
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

        HasData<SampleDataProxy> getSampleDataDisplay();

        void addColumns(List<PhenotypeUploadDataProxy> valueColumns);

        HasData<SampleDataProxy> getSummaryDisplay();

        void showExperimentEditor(boolean show);

        PhenotypeUploadWizardView.ExperimentUploadDataEditDriver getDriver();

        void showPhenotypeDetailPanel(int index);

        HasData<PhenotypeUploadDataProxy> getPhenotypeUploadList();

        void showFileUploadPanel();

        void resetFileUploadPanel();

        void setGeoChartData(Multiset<String> geochartData);

        void setPhenotypExplorerData(ImmutableSet<SampleDataProxy> data, int index);

        void setHistogramChartData(ImmutableListMultimap<String, Double> data);

        void scheduledLayout();

        void setMapData(FluentIterable<SampleDataProxy> dataFiltered);
    }

    protected ExperimentUploadDataProxy data = null;

    protected final CurrentUser currentUser;
    protected final ListDataProvider<PhenotypeUploadDataProxy> phenotypeUploadDataProvider = new ListDataProvider<PhenotypeUploadDataProxy>();
    protected final ListDataProvider<SampleDataProxy> sampleDataProvider = new ListDataProvider<SampleDataProxy>();
    protected final ListDataProvider<SampleDataProxy> summaryDataProvider = new ListDataProvider<>();
    private final SingleSelectionModel<PhenotypeUploadDataProxy> phenotypeSelectionModel = new SingleSelectionModel<PhenotypeUploadDataProxy>();
    protected final MyFactory appDataFactory;
    protected ExperimentManager experimentManager;
    protected ExperimentRequest ctx;
    private final Validator validator;
    private final GoogleAnalyticsManager analyticsManager;
    private AutoBean<ExperimentUploadDataProxy> receivedBean;


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
                    // only showOverviewPanel when there is data otherwise it was deselected by reset() function
                    if (data != null) {
                        getView().showOverViewPanel();
                    }
                    return;
                }
                int index = data.getPhenotypes().indexOf(selectedObj);
                getView().showPhenotypeDetailPanel(index);
                List<SampleDataProxy> phenotypeData = getSamplesForPhenotype(index);
                sampleDataProvider.setList(phenotypeData);
                setChartData(phenotypeData, index);
            }
        });

    }

    private void setChartData(List<SampleDataProxy> data, final int index) {
        final Function<SampleDataProxy, String> getNameFunction = new Function<SampleDataProxy, String>() {

            @Nullable
            @Override
            public String apply(@Nullable SampleDataProxy input) {
                return input.getPassportId().toString();
            }
        };

        final Function<SampleDataProxy, Double> getValueFunction = new Function<SampleDataProxy, Double>() {

            @Nullable
            @Override
            public Double apply(@Nullable SampleDataProxy input) {
                return Double.parseDouble(input.getValues().get(index));
            }
        };

        final FluentIterable<SampleDataProxy> dataFiltered = FluentIterable.from(data).filter(
                new Predicate<SampleDataProxy>() {
                    @Override
                    public boolean apply(@Nullable SampleDataProxy input) {
                        return input.isIdKnown() && !input.isParseError() && (input.getParseMask() & (1 << index + 1)) == 0;
                    }
                }
        );

        // filter samples with error then create a MultiMap and then transform the values from SampleData to Phenotype Value (Double)
        ImmutableListMultimap<String, Double> histogramData = ImmutableListMultimap.copyOf(Multimaps.transformValues(
                Multimaps.index(dataFiltered, getNameFunction),
                getValueFunction));
        getView().setHistogramChartData(histogramData);


        // GeoChart Data
        ImmutableMultiset.Builder<String> builder = ImmutableMultiset.builder();
        for (SampleDataProxy sample : data) {
            try {
                builder.add(sample.getCountry());
            } catch (NullPointerException e) {

            }
        }
        ImmutableMultiset<String> geochartData = builder.build();
        getView().setGeoChartData(geochartData);

        // MotionChart & Histogram Data
        getView().setPhenotypExplorerData(ImmutableSet.copyOf(data), index);
        getView().setMapData(dataFiltered);
        getView().scheduledLayout();
    }


    private List<SampleDataProxy> getSamplesForPhenotype(final int index) {
        return ImmutableList.copyOf(Iterables.filter(data.getSampleData(), new Predicate<SampleDataProxy>() {
            @Override
            public boolean apply(@Nullable SampleDataProxy object) {
                String value = object.getValues().get(index);
                return value != null && !value.isEmpty();
            }
        }));
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
                GoogleAnalyticsEvent.fire(getEventBus(), new GoogleAnalyticsEvent.GAEventData("Phenotype", var, "Experiment:" + response.getName()));
                analyticsManager.endTimingEvent("Phenotype", var, "OK");
            }

            @Override
            public void onFailure(ServerFailure message) {
                fireEvent(new LoadingIndicatorEvent(false));
                DisplayNotificationEvent.fireError(PhenotypeUploadWizardPresenterWidget.this, "Error", "Failed to save data");
                onEdit();
                String var = isIsaTabUpload() ? "ISATAB-Upload" : "Upload";
                analyticsManager.endTimingEvent("Phenotype", var, "ERROR");
                GoogleAnalyticsEvent.fire(getEventBus(), new GoogleAnalyticsEvent.GAEventData("Phenotype", "Error -" + var, "Error:" + message.getMessage()));
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
        checkValidation();
        phenotypeUploadDataProvider.refresh();
    }

    @Override
    public void deselectPhenotypeCard() {
        if (phenotypeSelectionModel.getSelectedObject() != null) {
            phenotypeSelectionModel.setSelected(phenotypeSelectionModel.getSelectedObject(), false);
        }
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

    public boolean isIsaTabUpload() {
        return experiment == null;
    }
}
