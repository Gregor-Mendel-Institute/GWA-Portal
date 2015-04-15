package com.gmi.nordborglab.browser.client.mvp.profile;

import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.ApplicationPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 30.10.13
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class ProfilePresenter extends Presenter<ProfilePresenter.MyView, ProfilePresenter.MyProxy> implements ProfileUiHandlers {

    public interface MyView extends View, HasUiHandlers<ProfileUiHandlers> {

        void setAvatarUrl(String gravatarUrl);

        void setName(String name);

        void setMemberSince(Date date);

        void setUserType(String type);

        void displayStats(int numberOfStudies, int numberOfPhenotypes, int numberOfAnalysis);

        HasData<ExperimentProxy> getExperimentDisplay();

        HasData<PhenotypeProxy> getPhenotypeDisplay();

        HasData<StudyProxy> getStudyDisplay();

        void setActiveType(TYPE type);

        void setEditUrl(String editUrl);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.profile)
    public interface MyProxy extends ProxyPlace<ProfilePresenter> {
    }

    private final CurrentUser currentUser;

    private final CustomRequestFactory rf;
    private final PlaceManager placeManager;

    private final AsyncDataProvider<ExperimentProxy> experimentDataProvider = new AsyncDataProvider<ExperimentProxy>() {
        @Override
        protected void onRangeChanged(HasData<ExperimentProxy> display) {
            requestExperiments();
        }
    };

    private final AsyncDataProvider<PhenotypeProxy> phenotypeDataProvider = new AsyncDataProvider<PhenotypeProxy>() {
        @Override
        protected void onRangeChanged(HasData<PhenotypeProxy> display) {
            requestPhenotypes();
        }
    };


    private final AsyncDataProvider<StudyProxy> studyDataProvider = new AsyncDataProvider<StudyProxy>() {
        @Override
        protected void onRangeChanged(HasData<StudyProxy> display) {
            requestStudies();
        }
    };

    private void requestExperiments() {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<ExperimentPageProxy> receiver = new Receiver<ExperimentPageProxy>() {
            @Override
            public void onSuccess(ExperimentPageProxy experiments) {
                fireEvent(new LoadingIndicatorEvent(false));
                experimentDataProvider.updateRowCount((int) experiments.getTotalElements(), true);
                experimentDataProvider.updateRowData(getView().getExperimentDisplay().getVisibleRange().getStart(), experiments.getContents());
            }
        };
        Range range = getView().getExperimentDisplay().getVisibleRange();
        rf.userRequest().findExperiments(user.getId(), range.getStart(), range.getLength()).with("contents.ownerUser").fire(receiver);
    }

    private void requestPhenotypes() {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<PhenotypePageProxy> receiver = new Receiver<PhenotypePageProxy>() {
            @Override
            public void onSuccess(PhenotypePageProxy phenotypes) {
                fireEvent(new LoadingIndicatorEvent(false));
                phenotypeDataProvider.updateRowCount((int) phenotypes.getTotalElements(), true);
                phenotypeDataProvider.updateRowData(getView().getPhenotypeDisplay().getVisibleRange().getStart(), phenotypes.getContents());
            }
        };
        Range range = getView().getExperimentDisplay().getVisibleRange();
        rf.userRequest().findPhenotypes(user.getId(), range.getStart(), range.getLength()).with("contents.traitOntologyTerm", "contents.environOntologyTerm", "contents.experiment", "contents.ownerUser").fire(receiver);
    }

    private void requestStudies() {
        fireEvent(new LoadingIndicatorEvent(true));
        Receiver<StudyPageProxy> receiver = new Receiver<StudyPageProxy>() {
            @Override
            public void onSuccess(StudyPageProxy studies) {
                fireEvent(new LoadingIndicatorEvent(false));
                studyDataProvider.updateRowCount((int) studies.getTotalElements(), true);
                studyDataProvider.updateRowData(getView().getStudyDisplay().getVisibleRange().getStart(), studies.getContents());
            }
        };
        Range range = getView().getExperimentDisplay().getVisibleRange();
        rf.userRequest().findStudies(user.getId(), range.getStart(), range.getLength()).with("contents.alleleAssay", "contents.protocol", "contents.phenotype.experiment", "contents.job", "contents.ownerUser", "contents.transformation").fire(receiver);
    }


    public static enum TYPE {STUDY, PHENOTYPE, ANALYSIS;}

    private TYPE currentType = TYPE.STUDY;

    @Inject
    public ProfilePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                            final CurrentUser currentUser, final CustomRequestFactory rf,
                            final PlaceManager placeManager) {
        super(eventBus, view, proxy, ApplicationPresenter.TYPE_SetMainContent);
        getView().setUiHandlers(this);
        this.rf = rf;
        this.currentUser = currentUser;
        this.placeManager = placeManager;
    }

    private AppUserProxy user;

    @Override
    protected void onBind() {
        super.onBind();
    }


    @Override
    protected void onReset() {
        super.onReset();
        updateView();

    }

    private void updateView() {
        getView().setAvatarUrl(CurrentUser.getGravatarUrl(user, 80, true));
        getView().setName(user.getFirstname() + " " + user.getLastname());
        getView().setUserType(CurrentUser.isAdmin(user) ? "Admin" : "User");
        getView().setMemberSince(user.getRegistrationdate());
        getView().displayStats(user.getNumberOfStudies(), user.getNumberOfPhenotypes(), user.getNumberOfAnalysis());
        String editUrl = null;
        if (currentUser.isAdmin()) {
            PlaceRequest editRequest = new PlaceRequest.Builder()
                    .nameToken(NameTokens.account)
                    .with("id", user.getId().toString()).build();
            editUrl = "#" + placeManager.buildHistoryToken(editRequest);
        }
        getView().setEditUrl(editUrl);
        updateGrids();
    }

    private void updateGrids() {
        switch (currentType) {
            case STUDY:
                if (!experimentDataProvider.getDataDisplays().contains(getView().getExperimentDisplay())) {
                    experimentDataProvider.addDataDisplay(getView().getExperimentDisplay());
                }
                break;
            case PHENOTYPE:
                if (!phenotypeDataProvider.getDataDisplays().contains(getView().getPhenotypeDisplay())) {
                    phenotypeDataProvider.addDataDisplay(getView().getPhenotypeDisplay());
                }
                break;
            case ANALYSIS:
                if (!studyDataProvider.getDataDisplays().contains(getView().getStudyDisplay())) {
                    studyDataProvider.addDataDisplay(getView().getStudyDisplay());
                }
                break;
        }
        getView().setActiveType(currentType);
    }


    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);

        try {
            Long userId = Long.valueOf(placeRequest.getParameter("id", null));
            // check if trying to load a userid of a different user and is not admin
            if (user == null || !user.getId().equals(userId)) {
                rf.userRequest().findUserWithStats(userId).fire(new Receiver<AppUserProxy>() {
                    @Override
                    public void onSuccess(AppUserProxy response) {
                        resetGrids();
                        user = response;
                        getProxy().manualReveal(ProfilePresenter.this);
                    }


                    @Override
                    public void onFailure(ServerFailure error) {
                        getProxy().manualRevealFailed();
                        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.home).build());
                    }
                });
            } else {
                getProxy().manualReveal(ProfilePresenter.this);
                ;
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.home).build());
        }
    }

    private void resetGrids() {
        if (experimentDataProvider.getDataDisplays().contains(getView().getExperimentDisplay())) {
            experimentDataProvider.removeDataDisplay(getView().getExperimentDisplay());
        }

        if (phenotypeDataProvider.getDataDisplays().contains(getView().getPhenotypeDisplay())) {
            phenotypeDataProvider.removeDataDisplay(getView().getPhenotypeDisplay());
        }

        if (studyDataProvider.getDataDisplays().contains(getView().getStudyDisplay())) {
            studyDataProvider.removeDataDisplay(getView().getStudyDisplay());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void onChangeType(TYPE type) {
        currentType = type;
        updateGrids();
    }
}
