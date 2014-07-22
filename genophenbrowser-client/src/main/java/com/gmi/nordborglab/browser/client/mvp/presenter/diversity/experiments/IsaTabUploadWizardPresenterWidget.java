package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import com.gmi.nordborglab.browser.client.dto.MyFactory;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadFinishedEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadStartEvent;
import com.gmi.nordborglab.browser.client.events.IsaTabUploadSavedEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.IsaTabUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.IsaTabUploadWizardView;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.util.AutoBeanCloneUtils;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadValueProxy;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.gmi.nordborglab.browser.shared.service.PhenotypeRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import elemental.html.File;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IsaTabUploadWizardPresenterWidget extends PresenterWidget<IsaTabUploadWizardPresenterWidget.MyView> implements IsaTabUploadWizardUiHandlers {


    public interface MyView extends View, HasUiHandlers<IsaTabUploadWizardUiHandlers> {

        HandlerRegistration addFileUploadFinishedEvent(FileUploadFinishedEvent.FileUploadFinishedHandler handler);

        HandlerRegistration addFileUploadStartEvent(FileUploadStartEvent.FileUploadStartHandler handler);

        void showFileUploadError(File file);

        void showResultPanel();

        HasData<PhenotypeUploadDataProxy> getPhenotypeUploadList();

        void showStudyDetailPanel();

        void showPhenotypeDetailPanel(int index);

        void setErrorCount(int totalCount, int count);

        IsaTabUploadWizardView.ExperimentUploadDataEditDriver getDriver();

        HasData<PhenotypeUploadValueProxy> getPhenotypeValueDisplay();

        void showFileUploadPanel();

        void resetFileUploadPanel();

        void addColumns(List<String> header);


    }

    protected ExperimentRequest ctx;

    private final ExperimentManager experimentManager;
    protected final MyFactory appDataFactory;
    protected final CurrentUser currentUser;
    protected ExperimentUploadDataProxy data = null;
    protected AutoBean<ExperimentUploadDataProxy> receivedBean = null;
    protected final ListDataProvider<PhenotypeUploadDataProxy> phenotypeUploadDataProvider = new ListDataProvider<PhenotypeUploadDataProxy>();
    protected final ListDataProvider<PhenotypeUploadValueProxy> phenotypeValueDataProvider = new ListDataProvider<PhenotypeUploadValueProxy>();
    private final SingleSelectionModel<PhenotypeUploadDataProxy> phenotypeSelectionModel = new SingleSelectionModel<PhenotypeUploadDataProxy>();

    @Inject
    public IsaTabUploadWizardPresenterWidget(EventBus eventBus, MyView view,
                                             final ExperimentManager experimentManager,
                                             final MyFactory appDataFactory,
                                             final CurrentUser currentUser) {
        super(eventBus, view);
        getView().setUiHandlers(this);
        this.experimentManager = experimentManager;
        this.appDataFactory = appDataFactory;
        this.currentUser = currentUser;
        getView().getPhenotypeUploadList().setSelectionModel(phenotypeSelectionModel);
        phenotypeUploadDataProvider.addDataDisplay(getView().getPhenotypeUploadList());
        phenotypeValueDataProvider.addDataDisplay(getView().getPhenotypeValueDisplay());
        phenotypeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                // get the changes and update the phenotype list
                getView().getDriver().flush();
                PhenotypeUploadDataProxy selectedObj = phenotypeSelectionModel.getSelectedObject();
                if (selectedObj == null) {
                    getView().showStudyDetailPanel();
                    return;
                }
                int index = data.getPhenotypes().indexOf(selectedObj);
                PhenotypeUploadDataProxy phenotype = data.getPhenotypes().get(index);
                getView().showPhenotypeDetailPanel(index);
                getView().addColumns(phenotype.getValueHeader());
                phenotypeValueDataProvider.setList(phenotype.getPhenotypeUploadValues());
                // set editor
            }
        });
    }

    @Override
    public void updatePhenotypeData() {
        getView().getDriver().flush();
        phenotypeUploadDataProvider.refresh();
    }


    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getView().addFileUploadFinishedEvent(new FileUploadFinishedEvent.FileUploadFinishedHandler() {

            @Override
            public void onFileUploadFinished(FileUploadFinishedEvent event) {
                if (event.getResponseText().equals("")) {
                    getView().showFileUploadError(event.getFile());
                } else {
                    onUploadSuccessful(event.getResponseText());
                }
            }
        }));

        registerHandler(getView().addFileUploadStartEvent(new FileUploadStartEvent.FileUploadStartHandler() {
            @Override
            public void onFileUploadStart(FileUploadStartEvent event) {
                fireEvent(new LoadingIndicatorEvent(true, "Uploading..."));
            }
        }));
    }

    private void onUploadSuccessful(String responseText) {
        receivedBean = AutoBeanCodex.decode(appDataFactory, ExperimentUploadDataProxy.class, responseText);
        onEditExperiment();
        fireEvent(new LoadingIndicatorEvent(false));
    }

    private int countPhenotypesWithErrors() {
        return Iterables.size(Iterables.filter(data.getPhenotypes(), new Predicate<PhenotypeUploadDataProxy>() {
            @Override
            public boolean apply(@Nullable PhenotypeUploadDataProxy input) {
                return input.getErrorValueCount() > 0;
            }
        }));
    }

    private void showUploadData() {
        // show result panel
        getView().showResultPanel();
        getView().setErrorCount(data.getPhenotypes().size(), countPhenotypesWithErrors());
        phenotypeUploadDataProvider.setList(data.getPhenotypes());
    }

    private void cloneUploadData() {
        data = AutoBeanCloneUtils.cloneExperimentUploadData(receivedBean, ctx, currentUser.getAppData().getUnitOfMeasureList());
    }

    private void onEditExperiment() {
        ctx = experimentManager.getContext();
        cloneUploadData();
        showUploadData();
        getView().getDriver().edit(data, ctx);
        ///TODO Fix this better.
        List<String> paths = ImmutableList.<String>builder().addAll(Arrays.asList(getView().getDriver().getPaths())).build();
        ctx.saveExperimentUploadData(data).with(paths.toArray(new String[0])).to(new Receiver<ExperimentProxy>() {
            @Override
            public void onSuccess(ExperimentProxy response) {
                fireEvent(new LoadingIndicatorEvent(false));
                IsaTabUploadSavedEvent.fire(getEventBus(), response);
            }

            @Override
            public void onFailure(ServerFailure message) {
                fireEvent(new LoadingIndicatorEvent(false));
                DisplayNotificationEvent.fireError(IsaTabUploadWizardPresenterWidget.this, "Error", "Failed to save ISA-Tab");
                onEditExperiment();
            }
        });
    }

    public void save() {
        getView().getDriver().flush();
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
        // fire event
        ctx.fire();
    }

    public void reset() {
        data = null;
        ctx = null;
        receivedBean = null;
        phenotypeUploadDataProvider.setList(Lists.<PhenotypeUploadDataProxy>newArrayList());
        getView().resetFileUploadPanel();
        getView().showFileUploadPanel();
    }
}
