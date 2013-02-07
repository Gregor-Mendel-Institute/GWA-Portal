package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadWizardPresenterWidget extends PresenterWidget<PhenotypeUploadWizardPresenterWidget.MyView>{

    public interface MyView extends View {
    }

    @Inject
    public PhenotypeUploadWizardPresenterWidget(EventBus eventBus, MyView view) {
        super(eventBus, view);
    }


}
