package com.gmi.nordborglab.browser.client.gin;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.MetaAnalysisGenePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.MetaAnalysisTopResultsPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology.TraitOntologyPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication.PublicationDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication.PublicationOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.*;
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
import com.gmi.nordborglab.browser.client.mvp.presenter.main.AccountPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.ProfilePresenter;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.SimpleTabPanel;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.gmi.nordborglab.browser.shared.service.HelperFactory;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;


@GinModules({ClientDispatchModule.class, ClientModule.class})
public interface ClientGinjector extends Ginjector {
    EventBus getEventBus();

    MainResources getMainResources();

    PlaceManager getPlaceManager();

    Provider<MainPagePresenter> getMainPagePresenter();

    Provider<HomePresenter> getHomePresenter();

    AsyncProvider<DashboardPresenter> getDashboardPresenter();

    AsyncProvider<DiversityPresenter> getDiversityPresenter();

    AsyncProvider<ExperimentsOverviewPresenter> getExperimentsOverviewPresenter();

    AsyncProvider<ExperimentDetailPresenter> getExperimentDetailPresenter();

    SimpleTabPanel getSimpleTabPanel();

    AsyncProvider<ExperimentsOverviewTabPresenter> getExperimentsOverviewTabPresenter();

    AsyncProvider<ExperimentDetailTabPresenter> getExperimentDetailTabPresenter();

    AsyncProvider<PhenotypeListPresenter> getPhenotypeListPresenter();

    AsyncProvider<PhenotypeDetailTabPresenter> getPhenotypeDetailTabPresenter();

    AsyncProvider<PhenotypeDetailPresenter> getPhenotypeDetailPresenter();

    AsyncProvider<ObsUnitPresenter> getObsUnitPresenter();

    AsyncProvider<StudyListPresenter> getStudyListPresenter();

    AsyncProvider<StudyTabPresenter> getStudyTabPresenter();

    AsyncProvider<StudyDetailPresenter> getStudyDetailPresenter();

    AsyncProvider<StudyGWASPlotPresenter> getStudyGWASPlotPresenter();

    IsLoggedInGatekeeper getLoggedInGatekeeper();

    CurrentUser getCurrentUser();

    AppUserFactory getAppUserFactory();

    AsyncProvider<StudyWizardPresenter> getStudyWizardPresenter();

    HelperFactory getHelperFactory();

    AsyncProvider<GermplasmPresenter> getGermplasmPresenter();

    AsyncProvider<TaxonomyOverviewPresenter> getTaxonomyOverviewPresenter();

    AsyncProvider<TaxonomyDetailPresenter> getTaxonomyDetailPresenter();

    AsyncProvider<PassportListPresenter> getPasportListPresenter();

    AsyncProvider<PassportDetailPresenter> getPassportDetailPresenter();

    AsyncProvider<StockDetailPresenter> getStockDetailPresenter();

    AsyncProvider<PhenotypeOverviewPresenter> getPhenotypeOverviewPresenter();

    AsyncProvider<StudyOverviewPresenter> getStudyOverviewPresenter();

    AsyncProvider<TraitOntologyPresenter> getOntologyOverviewPresenter();

    AsyncProvider<BasicStudyWizardPresenter> getBasicStudyWizardPresenter();

    AsyncProvider<GWASViewerPresenter> getGWASViewerPresenter();

    AsyncProvider<PublicationOverviewPresenter> getPublicationOverviewPresenter();

    AsyncProvider<PublicationDetailPresenter> getPublicationDetailPresenter();

    AsyncProvider<MetaAnalysisGenePresenter> getMetaAnalysisGenePresenter();

    AsyncProvider<MetaAnalysisTopResultsPresenter> getMetaAnalysisTopResultsPresenter();

    AsyncProvider<GenomeBrowserPresenter> getGenomeBrowserPresenter();

    Provider<HomeTabPresenter> getHomeTabPresenter();

    AsyncProvider<CandidateGeneListPresenter> getCandidateGeneListPresenter();

    AsyncProvider<CandidateGeneListDetailPresenter> getCandidateGeneListDetailPresenter();

    Provider<ProfilePresenter> getProfilePresenter();

    Provider<AccountPresenter> getAccountPresenter();
}
