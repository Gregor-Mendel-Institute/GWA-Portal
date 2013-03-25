package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.dto.MyFactory;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PhenotypeUploadedEvent;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeUploadWizardView;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.shared.service.PhenotypeRequest;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadWizardPresenterWidget extends PresenterWidget<PhenotypeUploadWizardPresenterWidget.MyView> implements PhenotypeUploadWizardUiHandlers{

    private ExperimentProxy experiment;

    public interface MyView extends View,HasUiHandlers<PhenotypeUploadWizardUiHandlers> {

        void showPhenotypeValuePanel(PhenotypeUploadDataProxy data);

        void setUnitOfMeasureList(List<UnitOfMeasureProxy> unitOfMeasureList);

        HasData<PhenotypeUploadValueProxy> getPhenotypeValueDisplay();
        void addColumns(List<String> valueColumns);

        void showPhenotypeUploadPanel();


        void showConstraintViolations();

        PhenotypeUploadWizardView.PhenotypeDriver getDriver();
    }

    protected PhenotypeUploadDataProxy data = null;

    protected final CurrentUser currentUser;
    private final PhenotypeManager phenotypeManager;
    protected final ListDataProvider<PhenotypeUploadValueProxy> phentoypeValueDataProvider = new ListDataProvider<PhenotypeUploadValueProxy>();

    protected final MyFactory appDataFactory ;
    protected PhenotypeRequest ctx;
    private final Validator validator;

    @Inject
    public PhenotypeUploadWizardPresenterWidget(EventBus eventBus, MyView view,
                                                final CurrentUser currentUser,
                                                final MyFactory appDataFactory,
                                                final PhenotypeManager phenotypeManager) {
        super(eventBus, view);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        getView().setUiHandlers(this);
        this.currentUser = currentUser;
        this.phenotypeManager = phenotypeManager;
        this.appDataFactory = appDataFactory;
        phentoypeValueDataProvider.addDataDisplay(getView().getPhenotypeValueDisplay());

    }
    @Override
    public void onUploadFinished(String responseText) {
        ctx = phenotypeManager.getContext();
        AutoBean<PhenotypeUploadDataProxy> receivedBean = AutoBeanCodex.decode(appDataFactory, PhenotypeUploadDataProxy.class, responseText);
        clonePhenotypeUploadData(receivedBean);
        /*AutoBean<PhenotypeUploadDataProxy> newBean = AutoBeanUtils.getAutoBean(data);
        AutoBeanCodex.decodeInto(AutoBeanCodex.encode(receivedBean), newBean);*/
        fireEvent(new LoadingIndicatorEvent(false));
        showPhenotypeUploadData();
    }

    private void clonePhenotypeUploadData(AutoBean<PhenotypeUploadDataProxy> receivedBean) {
        PhenotypeUploadDataProxy newData = ctx.create(PhenotypeUploadDataProxy.class);
        AutoBean<PhenotypeUploadDataProxy> newBean = AutoBeanUtils.getAutoBean(newData);
        AutoBeanCodex.decodeInto(AutoBeanCodex.encode(receivedBean), newBean);
        data = newBean.as();
        List<PhenotypeUploadValueProxy> values = new ArrayList<PhenotypeUploadValueProxy>();
        for (PhenotypeUploadValueProxy value: receivedBean.as().getPhenotypeUploadValues()) {
            AutoBean<PhenotypeUploadValueProxy> newValueBean = AutoBeanUtils.getAutoBean(ctx.create(PhenotypeUploadValueProxy.class));
            AutoBeanCodex.decodeInto(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(value)),newValueBean);
            values.add(newValueBean.as());
        }
        data.setTraitUom(ctx.create(PhenotypeProxy.class));
        data.getTraitUom().setLocalTraitName(data.getName());
        data.getTraitUom().setTraitProtocol(data.getProtocol());
        data.getTraitUom().setUnitOfMeasure(getUnitOfMeasureFromName(data.getUnitOfMeasure()));
        data.getTraitUom().setToAccession(data.getTraitOntology());
        data.getTraitUom().setEoAccession(data.getEnvironmentOntology());
        data.setPhenotypeUploadValues(values);
    }

    private void showPhenotypeUploadData() {
        getView().showPhenotypeValuePanel(data);
        getView().getDriver().edit(data.getTraitUom(),ctx);
        getView().addColumns(data.getValueHeader());
        phentoypeValueDataProvider.setList(data.getPhenotypeUploadValues());
    }



    @Override
    public void onUploadError(String responseText) {
        fireEvent(new LoadingIndicatorEvent(false));
        DisplayNotificationEvent.fireError(this, "Error uploading phentoype file", responseText);
    }

    @Override
    public void onCancel() {
        data = null;
        ctx = null;
        phentoypeValueDataProvider.getList().clear();
        getView().showPhenotypeUploadPanel();
    }

    @Override
    public void onCreate() {
       savePhenotype();
    }

    @Override
    public void startUpload() {
        fireEvent(new LoadingIndicatorEvent(true));
    }


    private void savePhenotype() {
        //TODO switch to editor

        if (checkValidation()) {
            fireEvent(new LoadingIndicatorEvent(true,"Saving..."));
            ctx.savePhenotypeUploadData(experiment.getId(), data).fire(new Receiver<Long>() {
                @Override
                public void onFailure(ServerFailure error) {
                    fireEvent(new LoadingIndicatorEvent(false));
                    fireEvent(new DisplayNotificationEvent("Phenotype upload",error.getMessage(),true,DisplayNotificationEvent.LEVEL_ERROR,DisplayNotificationEvent.DURATION_NORMAL));
                }

                @Override
                public void onSuccess(Long phentoypeId) {
                    fireEvent(new LoadingIndicatorEvent(false));
                    onCancel();
                    PhenotypeUploadedEvent.fire(getEventBus(), phentoypeId);
                }
            });
        }
    }

    @Override
    protected void onBind() {
        super.onBind();    //To change body of overridden methods use File | Settings | File Templates.
        getView().setUnitOfMeasureList(currentUser.getAppData().getUnitOfMeasureList());
    }

    //TODO return directly from server
    private UnitOfMeasureProxy getUnitOfMeasureFromName(String name) {
        if (name == null)
            return null;
        for (UnitOfMeasureProxy unitOfMeasure:currentUser.getAppData().getUnitOfMeasureList()) {

            if (unitOfMeasure != null && unitOfMeasure.getUnitType().equalsIgnoreCase(name)) {
                return unitOfMeasure;
            }
        }
        return null;
    }


    public void save() {
        onCreate();
    }

    public void setExperiment(ExperimentProxy experiment) {
        this.experiment = experiment;
    }

    public boolean checkUploadOk() {
         return checkValidation();
    }

    private boolean checkValidation() {
        boolean isOk = false;
        getView().getDriver().flush();
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
                .validate(data.getTraitUom(), Default.class);
        if (!violations.isEmpty()) {
            getView().getDriver().setConstraintViolations(
                    violations);
            isOk = false;
        }
        else {
            isOk = true;
        }
        return isOk;
    }

}
