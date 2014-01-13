package com.gmi.nordborglab.browser.client.gin;

import at.gmi.nordborglab.widgets.geneviewer.client.datasource.DataSource;
import at.gmi.nordborglab.widgets.geneviewer.client.datasource.LocalStorageImpl;
import at.gmi.nordborglab.widgets.geneviewer.client.datasource.LocalStorageImpl.TYPE;
import at.gmi.nordborglab.widgets.geneviewer.client.datasource.impl.JBrowseCacheDataSourceImpl;
import com.eemi.gwt.tour.client.Placement;
import com.eemi.gwt.tour.client.Tour;
import com.eemi.gwt.tour.client.TourStep;
import com.eemi.gwt.tour.client.jso.Function;
import com.gmi.nordborglab.browser.client.manager.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology.TraitOntologyPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication.PublicationDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication.PublicationOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASPlotPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASViewerPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.genotype.genome.GenomeBrowserPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.GermplasmPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport.PassportDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport.PassportListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.stock.StockDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.taxonomy.TaxonomyDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.taxonomy.TaxonomyOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.BasicStudyWizardPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.HomePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.HomeTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.dashboard.DashboardPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.*;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.CandidateGeneListEnrichmentView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.DiversityView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.PermissionDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.*;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.*;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology.TraitOntologyView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.*;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.publication.PublicationDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.publication.PublicationOverviewView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.*;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.tools.GWASPlotView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.tools.GWASUploadWizardView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.tools.GWASViewerView;
import com.gmi.nordborglab.browser.client.mvp.view.genotype.genome.GenomeBrowserView;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.GermplasmView;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportListView;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.stock.StockDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.taxonomy.TaxonomyDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.taxonomy.TaxonomyOverviewView;
import com.gmi.nordborglab.browser.client.mvp.view.home.BasicStudyWizardView;
import com.gmi.nordborglab.browser.client.mvp.view.home.HomeTabView;
import com.gmi.nordborglab.browser.client.mvp.view.home.HomeView;
import com.gmi.nordborglab.browser.client.mvp.view.home.dashboard.DashboardView;
import com.gmi.nordborglab.browser.client.mvp.view.main.*;
import com.gmi.nordborglab.browser.client.mvp.view.widgets.*;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.security.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.validation.ClientValidation;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.HelperFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.storage.client.Storage;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.client.actionhandler.caching.Cache;
import com.gwtplatform.dispatch.client.actionhandler.caching.DefaultCacheImpl;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.GaAccount;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.gwtplatform.mvp.client.googleanalytics.GoogleAnalyticsNavigationTracker;
import com.gwtplatform.mvp.client.proxy.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

import javax.inject.Provider;
import java.util.List;

public class ClientModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        install(new DefaultModule(DefaultPlaceManager.class, RouteTokenFormatter.class));
        install(new ClientDispatchModule());
        install(new GinFactoryModuleBuilder().build(AssistedInjectionFactory.class));
        install(new ApplicationModule());

        bind(ResourceLoader.class).asEagerSingleton();
        bind(IsLoggedInGatekeeper.class).in(Singleton.class);

        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.home);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.home);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.home);
        bindConstant().annotatedWith(GaAccount.class).to("UA-26150757-2");

        bind(GoogleAnalyticsNavigationTracker.class).asEagerSingleton();

        bind(CurrentUser.class).asEagerSingleton();
        bind(ClientValidation.class).in(Singleton.class);
        bind(Cache.class).to(DefaultCacheImpl.class).in(Singleton.class);
        bind(MainResources.class).in(Singleton.class);
        bind(ExperimentManager.class).in(Singleton.class);
        bind(PhenotypeManager.class).in(Singleton.class);
        bind(HelperManager.class).in(Singleton.class);
        bind(ObsUnitManager.class).in(Singleton.class);
        bind(CdvManager.class).in(Singleton.class);
        bind(FlagMap.class).in(Singleton.class);

        bind(AppUserFactory.class).asEagerSingleton();
        bind(HelperFactory.class).asEagerSingleton();
    }


    @Provides
    @Singleton
    @Named("metatopsnps")
    public List<FilterItemPresenterWidget> getFiltersForMetaTopSnps(Provider<TextBoxFilterItemPresenterWidget> textBoxFilterProvider) {
        List<FilterItemPresenterWidget> filters = Lists.newArrayList();
        FilterItemPresenterWidget studyFilterWidget = textBoxFilterProvider.get();
        studyFilterWidget.setFilterType(ConstEnums.FILTERS.STUDY);
        filters.add(studyFilterWidget);
        return filters;
    }


    @Provides
    @Singleton
    @Named("welcome")
    public Tour getWelconmeTour(final PlaceManager placeManager) {
        Tour tour = new Tour("usage-tour");
        tour.setShowCloseButton(true);
        //Welcome
        TourStep step = new TourStep(Placement.RIGHT, "tourBtn");
        step.onNext(new Function() {
            @Override
            public void execute() {
                String test = "test";
            }
        });
        step.setTitle("Welcome to the GWA-portal tour");
        step.setContent("This tour will show the basic functionality of GWA-Portal.");
        tour.addStep(step);

        // Nav Menu
        step = new TourStep(Placement.BOTTOM, "topNav");
        step.setTitle("Main navigation menu");
        step.setContent("Here you find the main navigation menu with different sections that contain different kinds of data");
        step.setYOffset(45);
        tour.addStep(step);


        //Login
        step = new TourStep(Placement.BOTTOM, "userMenuItem");
        step.setTitle("Account Information");
        step.setContent("After logging in users can view account information and recent notifications about GWAS analyses");
        tour.addStep(step);

        // News
        step = new TourStep(Placement.TOP, "news");
        step.setTitle("Recent News");
        step.setContent("Recent News shows all updates related to GWA-portal");
        tour.addStep(step);

        // Quickstats
        step = new TourStep(Placement.TOP, "quickstats");
        step.setTitle("Quick Stats");
        step.setContent("Quick Stats provides statistical information about the information and data that can be accessed through GWA-portal");
        tour.addStep(step);

        // chart
        step = new TourStep(Placement.TOP, "charts");
        step.setTitle("Charts");
        step.setContent("Recently published phenotypes, studies and GWAS analysis are shown over time");
        tour.addStep(step);

        // GWAPP
        step = new TourStep(Placement.LEFT, "GWAPP");
        step.setTitle("GWAPP");
        step.setContent("GWAPP is the predecessor of GWA-Portal and allows to quickly run GWAS on the fly on the 250k SNP dataset");
        tour.addStep(step);

        // Create GWAS study
        step = new TourStep(Placement.LEFT, "createGWAS");
        step.setTitle("Create a new GWAS analysis");
        step.setContent("Logged-in users can access a Wizard which allows them to create studies, upload phenotypes and run GWAS analysis.");
        tour.addStep(step);

        // Browse
        step = new TourStep(Placement.LEFT, "browsePhenotypes");
        step.setTitle("Browse studies, phenotypes and GWAS analysis");
        step.setContent("This will navigate to the <b>Phenotype</b> section. The user can view studies, phenotypes and GWAS analysis. It will contain both personal as well as public information.<br>Click on <b>continue</b> to navigate to the Phenotype section or <b>Done</b> to end tour");
        step.onNext(new Function() {
            @Override
            public void execute() {
                placeManager.revealPlace(new PlaceRequest.Builder()
                        .nameToken(NameTokens.experiments).build());
            }
        });

        //Phenotype section
        step.setMultiPage(true);
        tour.addStep(step);

        // FIXME only required because not possible to check state
        step = new TourStep(Placement.BOTTOM, "topNav");
        step.setTitle("test");
        step.setContent("test");
        tour.addStep(step);

        step = new TourStep(Placement.BOTTOM, "topNav");
        step.setXOffset(77);
        step.setYOffset(45);
        step.setTitle("Phenotype section");
        step.setContent("Here you can browse and view personal and public phenotypes, studies, GWAS analyses." +
                "<br>The information are stored hierarchically in categories:<br>Study -> Phenotype -> Analysis.<br>" +
                "By default the list of all studies that the user has access to is displayed.<br>The user has different ways to navigate through the data.");
        tour.addStep(step);

        // Search
        step = new TourStep(Placement.RIGHT, "globalSearchBox");
        step.setYOffset(-10);
        step.setTitle("Global search");
        step.setContent("The user can search for any term. The search will be carried out across all categories (Phenotypes, Analyses, Studies, Ontologies, etc)" +
                " and the information will be displayed grouped by category");
        tour.addStep(step);

        // Navigation
        step = new TourStep(Placement.RIGHT, "experimentAccGroup");
        step.setTitle("Navigation");
        step.setContent("The user can access information in the corresponding categories (Studies, Phenotypes, etc) via the side-navigation panel." +
                "<br>The side navigation panel can be hidden by clicking on the splitter between the navigation panel and the main content panel.<br>");
        tour.addStep(step);
        // Navigation
        step = new TourStep(Placement.BOTTOM, "breadcrumb");
        step.setTitle("Breadcrumbs");
        step.setContent("The breadcrumbs show the category and sub-category that are currently displayed in the main navigation panel");
        tour.addStep(step);

        // Navigation
        step = new TourStep(Placement.BOTTOM, "topNav");
        step.setYOffset(45);
        step.setXOffset(800);
        step.setArrowOffset(150);
        step.setTitle("End of Welcome Tour");
        step.setContent("You reached the end of the \"Welcome Tour\". Watch out for hint icons. They will show you contextual information");
        tour.addStep(step);
        return tour;
    }

    @Provides
    @Singleton
    public CustomRequestFactory createCustomRequestFactory(EventBus eventBus) {

        CustomRequestFactory factory = GWT.create(CustomRequestFactory.class);
        factory.initialize(eventBus);
        return factory;
    }

    @Provides
    @Singleton
    public DataSource createJBrowseDataSource() {
        at.gmi.nordborglab.widgets.geneviewer.client.datasource.Cache cache = null;
        if (Storage.isSupported()) {
            try {
                cache = new LocalStorageImpl(TYPE.SESSION);
            } catch (Exception e) {
            }
        } else {
            cache = new at.gmi.nordborglab.widgets.geneviewer.client.datasource.DefaultCacheImpl();
        }
        return new JBrowseCacheDataSourceImpl("/provider/genes/", cache);
    }

    @Provides
    @Singleton
    public RequestDispatcher getRequestDispatcher() {
        return ErraiBus.getDispatcher();
    }

    @Provides
    @Singleton
    public MessageBus getMessageBus() {
        return ErraiBus.get();
    }

    public interface AssistedInjectionFactory {

        EnrichmentProviderImpl createEnrichmentProvider(EnrichmentProvider.TYPE type);

        CandidateGeneListEnrichmentPresenterWidget createCandidateGeneListEnrichmentPresenter(final EnrichmentProvider dataProvider);

        CandidateGeneListEnrichmentPresenterWidgetView getCandidateGeneListEnrichmentView(final EnrichmentProvider.TYPE viewType);
    }
}
