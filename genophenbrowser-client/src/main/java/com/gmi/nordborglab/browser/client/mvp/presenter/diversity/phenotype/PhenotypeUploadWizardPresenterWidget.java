package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.dto.MyFactory;
import com.gmi.nordborglab.browser.client.dto.PhenotypeUploadData;
import com.gmi.nordborglab.browser.client.dto.PhenotypeValue;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadWizardPresenterWidget extends PresenterWidget<PhenotypeUploadWizardPresenterWidget.MyView> implements PhenotypeUploadWizardUiHandlers{

    public interface MyView extends View,HasUiHandlers<PhenotypeUploadWizardUiHandlers> {
        void showPhenotypeValuePanel(PhenotypeUploadData data, UnitOfMeasureProxy unitOfMeasure);

        void setUnitOfMeasureList(List<UnitOfMeasureProxy> unitOfMeasureList);

        HasData<PhenotypeValue> getPhenotypeValueDisplay();

        void addColumns(List<String> valueColumns);

        void showPhenotypeUploadPanel();
    }

    protected PhenotypeUploadData data = null;
    final protected CurrentUser currentUser;
    protected final ListDataProvider<PhenotypeValue> phentoypeValueDataProvider = new ListDataProvider<PhenotypeValue>();
    protected final MyFactory appDataFactory ;



    @Inject
    public PhenotypeUploadWizardPresenterWidget(EventBus eventBus, MyView view,
                                                final CurrentUser currentUser, final MyFactory appDataFactory) {
        super(eventBus, view);
        getView().setUiHandlers(this);
        this.currentUser = currentUser;
        this.appDataFactory = appDataFactory;
        phentoypeValueDataProvider.addDataDisplay(getView().getPhenotypeValueDisplay());

    }

    @Override
    public void onUploadFinished(String responseText) {
        data = AutoBeanCodex.decode(appDataFactory,PhenotypeUploadData.class,responseText).as();
        showPhenotypeUploadData();
    }

    private void showPhenotypeUploadData() {
        getView().showPhenotypeValuePanel(data, getUnitOfMeasureFromName(data.getUnitOfMeasure()));
        getView().addColumns(data.getValueHeader());
        phentoypeValueDataProvider.setList(data.getPhenotypeValues());
    }


    @Override
    public void onUploadError(String responseText) {
        DisplayNotificationEvent.fireError(this, "Error uploading phentoype file", responseText);
    }

    @Override
    public void onCancel() {
        data = null;
        phentoypeValueDataProvider.getList().clear();
        getView().showPhenotypeUploadPanel();
    }

    @Override
    public void onCreate() {
        //To change body of implemented methods use File | Settings | File Templates.
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

}
