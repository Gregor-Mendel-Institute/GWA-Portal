package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.detail;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadExperimentEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PermissionDoneEvent;
import com.gmi.nordborglab.browser.client.events.PlaceRequestEvent.PlaceRequestHandler;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.ExperimentDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.detail.ExperimentDetailView.ExperimentDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.detail.ExperimentDetailView.ExperimentEditDriver;
import com.gmi.nordborglab.browser.client.mvp.widgets.permissions.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ExperimentDetailPresenter
        extends
        Presenter<ExperimentDetailPresenter.MyView, ExperimentDetailPresenter.MyProxy>
        implements ExperimentDetailUiHandlers {

    public interface MyView extends com.gwtplatform.mvp.client.View,
            com.gwtplatform.mvp.client.HasUiHandlers<ExperimentDetailUiHandlers> {
        ExperimentEditDriver getExperimentEditDriver();

        ExperimentDisplayDriver getExperimentDisplayDriver();

        void showPermissionPanel(boolean show);

        HasData<PublicationProxy> getPublicationDisplay();

        void scheduledLayout();

        void phaseInPublication(PublicationProxy publicationProxy, ProvidesKey<PublicationProxy> providesKey);

        HasText getDOIText();

        void showEditPopup(boolean show);

        void showDeletePopup(boolean show);

        void showShareBtn(boolean show);

        void showActionBtns(boolean show);

        void setShareTooltip(String toopltipMsg, IconType icon);

        void displayStats(List<FacetProxy> stats, int numberOfPhenotypes, long numberOfAnalysis);

        void setDownloadLink(String url);
    }

    public static enum State {
        DISPLAYING, EDITING, SAVING;
    }

    @ProxyCodeSplit
    @TabInfo(container = ExperimentDetailTabPresenter.class, label = "Overview", priority = 0)
    @NameToken(NameTokens.experiment)
    public interface MyProxy extends
            TabContentProxyPlace<ExperimentDetailPresenter> {
    }

    @TitleFunction
    public String getTitle() {
        String title = null;
        if (experiment != null) {
            title = experiment.getName();
        }
        return title;
    }

    public static final Object TYPE_SetPermissionContent = new Object();
    private final PlaceManager placeManager;
    private final ExperimentManager experimentManager;
    private ExperimentProxy experiment;
    private ExperimentProxy editedExperiment;
    final CurrentUser currentUser;
    private ExperimentEditDriver editDriver = null;
    private Receiver<ExperimentProxy> receiver = null;
    protected boolean fireLoadExperimentEvent = false;
    private final PermissionDetailPresenter permissionDetailPresenter;
    public static Type<PlaceRequestHandler> type = new Type<PlaceRequestHandler>();
    private final ListDataProvider<PublicationProxy> publicationDataProvider = new ListDataProvider<PublicationProxy>();
    private final Validator validator;

    @Inject
    public ExperimentDetailPresenter(final EventBus eventBus,
                                     final MyView view, final MyProxy proxy,
                                     final PlaceManager placeManager,
                                     final ExperimentManager experimentManager,
                                     final CurrentUser currentUser,
                                     final PermissionDetailPresenter permissionDetailPresenter, final Validator validator) {
        super(eventBus, view, proxy, ExperimentDetailTabPresenter.TYPE_SetTabContent);
        this.validator = validator;
        getView().setUiHandlers(this);
        this.permissionDetailPresenter = permissionDetailPresenter;
        this.placeManager = placeManager;
        this.currentUser = currentUser;
        this.experimentManager = experimentManager;
        this.editDriver = getView().getExperimentEditDriver();
        receiver = new Receiver<ExperimentProxy>() {
            public void onSuccess(ExperimentProxy response) {
                fireEvent(new LoadingIndicatorEvent(false));
                experiment = response;
                getView().getExperimentDisplayDriver().display(experiment);
                getView().showEditPopup(false);
            }

            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                DisplayNotificationEvent.fireError(getEventBus(), "Error while saving", error.getMessage());
                onEdit();
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                fireEvent(new LoadingIndicatorEvent(false));
                getView().getExperimentEditDriver().setConstraintViolations(violations);
            }
        };

        publicationDataProvider.addDataDisplay(getView().getPublicationDisplay());
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(TYPE_SetPermissionContent, permissionDetailPresenter);
        registerHandler(getEventBus().addHandlerToSource(PermissionDoneEvent.TYPE, permissionDetailPresenter, new PermissionDoneEvent.Handler() {
            @Override
            public void onPermissionDone(PermissionDoneEvent event) {
                getView().showPermissionPanel(false);
            }
        }));
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (fireLoadExperimentEvent) {
            fireEvent(new LoadExperimentEvent(experiment));
            fireLoadExperimentEvent = false;
        }
        getView().getExperimentDisplayDriver().display(experiment);
        getView().showActionBtns(currentUser.hasEdit(experiment));
        getView().showShareBtn(currentUser.hasAdmin(experiment));

        String toolTipText = "Public - Anyone on the Internet can find and access";
        IconType toolTipIcon = IconType.GLOBE;
        if (!experiment.isPublic()) {
            toolTipText = "Private - Only people explicitly granted permission can access";
            toolTipIcon = IconType.LOCK;

        }
        getView().setShareTooltip(toolTipText, toolTipIcon);
        publicationDataProvider.setList(ImmutableList.copyOf(experiment.getPublications()));
        getView().displayStats(experiment.getStats(), experiment.getNumberOfPhenotypes(), experiment.getNumberOfAnalyses());
        getView().setDownloadLink(placeManager.buildHistoryToken(new PlaceRequest.Builder()
                .nameToken(NameTokens.isaTabDownload)
                .with("id", experiment.getId().toString())
                .with("name", experiment.getName().replace(" ", "_") + ".zip").build()));
        fireEvent(new LoadingIndicatorEvent(false));
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        Receiver<ExperimentProxy> receiver = new Receiver<ExperimentProxy>() {
            @Override
            public void onSuccess(ExperimentProxy exp) {
                experiment = exp;
                fireLoadExperimentEvent = true;
                getProxy().manualReveal(ExperimentDetailPresenter.this);
            }

            @Override
            public void onFailure(ServerFailure error) {
                getProxy().manualRevealFailed();
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
            }
        };
        try {
            Long experimentId = Long.valueOf(placeRequest.getParameter("id",
                    null));
            if (experiment == null || !experiment.getId().equals(experimentId)) {
                fireEvent(new LoadingIndicatorEvent(true));
                experimentManager.findOne(receiver, experimentId);
            } else {
                getProxy().manualReveal(ExperimentDetailPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void onEdit() {
        ExperimentRequest ctx = experimentManager.getRequestFactory()
                .experimentRequest();
        editedExperiment = ctx.edit(experiment);
        editDriver.edit(editedExperiment, ctx);
        ctx.save(editedExperiment).with("userPermission", "publications").to(receiver);
        getView().showEditPopup(true);
    }

    @Override
    public void onSave() {
        RequestContext req = editDriver.flush();
        if (!checkValidation())
            return;
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
        req.fire();
    }

    @Override
    public void onCancel() {
        getView().showEditPopup(false);
    }

    protected int getPermission() {
        assert experiment != null;
        int permission = 0;
        if (currentUser.isLoggedIn()) {
            if (experiment.getUserPermission() != null) {
                permission = experiment.getUserPermission().getMask();
            }
        }
        return permission;
    }


    @Override
    public void onDelete() {
        getView().showDeletePopup(true);
    }

    @Override
    public void onShare() {
        getView().showPermissionPanel(true);
        permissionDetailPresenter.setDomainObject(experiment, placeManager.buildHistoryToken(placeManager.getCurrentPlaceRequest()));
    }

    @Override
    public void onDeletePublication(PublicationProxy publication) {
        ExperimentRequest ctx = experimentManager.getContext();
        experiment = ctx.edit(experiment);
        publication = ctx.edit(publication);
        experiment.getPublications().remove(publication);
        ctx.save(experiment).with("userPermission", "publications").to(new Receiver<ExperimentProxy>() {
            @Override
            public void onSuccess(ExperimentProxy response) {
                experiment = response;
                publicationDataProvider.setList(Lists.newArrayList(experiment.getPublications()));
            }
        }).fire();
    }

    @Override
    public void queryDOI(String DOI) {
        fireEvent(new LoadingIndicatorEvent(true));
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "/doi/" + DOI);
        rb.setHeader("Accept", "application/vnd.citationstyles.csl+json");
        try {
            rb.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        try {
                            JSONObject object = JSONParser.parseStrict(response.getText()).isObject();
                            String volume = object.get("volume").isString().stringValue();
                            final String DOI = object.get("DOI").isString().stringValue();
                            String URL = object.get("URL").isString().stringValue();
                            String page = object.get("page").isString().stringValue();
                            String issue = object.get("issue").isString().stringValue();
                            String title = object.get("title").isString().stringValue();
                            String journal = object.get("container-title").isString().stringValue();
                            JSONObject authorObj = object.get("author").isArray().get(0).isObject();
                            String author = authorObj.get("given").isString().stringValue() + " " + authorObj.get("family").isString().stringValue();
                            JSONArray dateArr = object.get("issued").isObject().get("date-parts").isArray().get(0).isArray();
                            Date issued = new Date((int) dateArr.get(0).isNumber().doubleValue(), (int) dateArr.get(1).isNumber().doubleValue(), (int) dateArr.get(2).isNumber().doubleValue());
                            ExperimentRequest ctx = experimentManager.getContext();
                            PublicationProxy publication = ctx.create(PublicationProxy.class);
                            publication.setDOI(DOI);
                            publication.setTitle(title);
                            publication.setFirstAuthor(author);
                            publication.setIssue(issue);
                            publication.setJournal(journal);
                            publication.setPage(page);
                            publication.setURL(URL);
                            publication.setVolume(volume);
                            publication.setPubDate(issued);
                            ctx.addPublication(experiment.getId(), publication).with("userPermission", "publications").fire(new Receiver<ExperimentProxy>() {
                                @Override
                                public void onSuccess(ExperimentProxy response) {
                                    fireEvent(new LoadingIndicatorEvent(false));
                                    experiment = response;
                                    publicationDataProvider.setList(Lists.newArrayList(experiment.getPublications()));
                                    PublicationProxy newPublication = Iterables.get(Collections2.filter(experiment.getPublications(), new Predicate<PublicationProxy>() {
                                        @Override
                                        public boolean apply(@Nullable PublicationProxy input) {
                                            if (input != null && input.getDOI().equals(DOI)) {
                                                return true;
                                            }
                                            return false;
                                        }
                                    }), 0);
                                    getView().getDOIText().setText("");
                                    getView().phaseInPublication(newPublication, publicationDataProvider);
                                }

                                @Override
                                public void onFailure(ServerFailure error) {
                                    DisplayNotificationEvent.fireError(getEventBus(), "Publication", "Error saving publicaiton");
                                    fireEvent(new LoadingIndicatorEvent(false));
                                }
                            });
                        } catch (Exception e) {
                            DisplayNotificationEvent.fireError(getEventBus(), "DOI query failed", "Could not parse meta-data");
                            fireEvent(new LoadingIndicatorEvent(false));
                        }

                    } else if (response.getStatusCode() == 204) {
                        DisplayNotificationEvent.fireWarning(getEventBus(), "DOI query failed", "No metadata found");
                        fireEvent(new LoadingIndicatorEvent(false));
                    } else if (response.getStatusCode() == 404) {
                        DisplayNotificationEvent.fireWarning(getEventBus(), "DOI query failed", "DOI doesn't exist");
                        fireEvent(new LoadingIndicatorEvent(false));
                    } else {
                        DisplayNotificationEvent.fireError(getEventBus(), "DOI query failed", "General error");
                        fireEvent(new LoadingIndicatorEvent(false));
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    DisplayNotificationEvent.fireError(getEventBus(), "Error", exception.getMessage());
                    fireEvent(new LoadingIndicatorEvent(false));
                }
            });
        } catch (Exception e) {
            DisplayNotificationEvent.fireError(getEventBus(), "Error", e.getMessage());
            fireEvent(new LoadingIndicatorEvent(false));
        }
    }

    @Override
    public void onConfirmDelete() {
        fireEvent(new LoadingIndicatorEvent(true, "Removing..."));
        experimentManager.delete(new Receiver<Void>() {
            @Override
            public void onSuccess(Void response) {
                PlaceRequest request = null;
                if (placeManager.getHierarchyDepth() <= 1) {
                    request = new PlaceRequest.Builder().nameToken(NameTokens.experiments).build();
                } else {
                    request = placeManager.getCurrentPlaceHierarchy().get(placeManager.getHierarchyDepth() - 2);
                }
                getView().showDeletePopup(false);
                experiment = null;
                placeManager.revealPlace(request);
            }

            @Override
            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                super.onFailure(error);    //To change body of overridden methods use File | Settings | File Templates.
            }
        }, experiment);
    }

    private boolean checkValidation() {
        boolean isOk;
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set) validator
                .validate(editedExperiment, Default.class);
        if (!violations.isEmpty() || editDriver.hasErrors()) {
            isOk = false;
        } else {
            isOk = true;
        }
        getView().getExperimentEditDriver().setConstraintViolations(violations);
        return isOk;
    }

    @ProxyEvent
    public void onLoadExperiment(LoadExperimentEvent event) {
        experiment = event.getExperiment();
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", experiment.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(new TabDataDynamic(tabData.getLabel(), tabData.getPriority(), historyToken));
    }

    @Override
    protected void onReveal() {
        super.onReveal();    //To change body of overridden methods use File | Settings | File Templates.
        getView().scheduledLayout();
    }
}
