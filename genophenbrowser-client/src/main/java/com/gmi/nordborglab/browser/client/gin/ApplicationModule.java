package com.gmi.nordborglab.browser.client.gin;

import com.gmi.nordborglab.browser.client.mvp.presenter.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.DiversityPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentCandidateGeneListEnrichmentPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentsOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentsOverviewTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.PhenotypeListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.MetaAnalysisGenePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.MetaAnalysisTopResultsPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology.TraitOntologyPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeCandidateGeneListEnrichmentPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.StudyListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication.PublicationDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication.PublicationOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.SNPDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyCandidateGeneListEnrichmentPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyGWASPlotPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyWizardPresenter;
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
import com.gmi.nordborglab.browser.client.mvp.presenter.main.AccountPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.MainPagePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.ProfilePresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.SearchPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.UserListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.DropDownFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FilterPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.TextBoxFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.TypeaheadFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.CandidateGeneListEnrichmentView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.DiversityView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.PermissionDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentDetailTabView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentsOverviewTabView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentsOverviewView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.PhenotypeListView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.CandidateGeneListDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.CandidateGeneListView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.MetaAnalysisGeneView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.meta.MetaAnalysisTopResultsView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.ontology.TraitOntologyView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeDetailTabView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeOverviewView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeUploadWizardView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.StudyListView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.publication.PublicationDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.publication.PublicationOverviewView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.SNPDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyDetailView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyGWASPlotView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyOverviewView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyTabView;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyWizardView;
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
import com.gmi.nordborglab.browser.client.mvp.view.main.AccountView;
import com.gmi.nordborglab.browser.client.mvp.view.main.MainPageView;
import com.gmi.nordborglab.browser.client.mvp.view.main.ProfileView;
import com.gmi.nordborglab.browser.client.mvp.view.main.SearchView;
import com.gmi.nordborglab.browser.client.mvp.view.main.UserListView;
import com.gmi.nordborglab.browser.client.mvp.view.widgets.DropDownFilterItemPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.view.widgets.FacetSearchPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.view.widgets.FilterPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.view.widgets.TextBoxFilterItemPresenterWidgetView;
import com.gmi.nordborglab.browser.client.mvp.view.widgets.TypeaheadFilterItemPresenterWidgetView;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 13.01.14.
 */
public class ApplicationModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(MainPagePresenter.class, MainPagePresenter.MyView.class,
                MainPageView.class, MainPagePresenter.MyProxy.class);

        bindPresenter(HomePresenter.class, HomePresenter.MyView.class,
                HomeView.class, HomePresenter.MyProxy.class);

        bindPresenter(AccountPresenter.class,
                AccountPresenter.MyView.class, AccountView.class, AccountPresenter.MyProxy.class);

        bindPresenter(DashboardPresenter.class,
                DashboardPresenter.MyView.class, DashboardView.class,
                DashboardPresenter.MyProxy.class);

        bindPresenter(DiversityPresenter.class,
                DiversityPresenter.MyView.class, DiversityView.class,
                DiversityPresenter.MyProxy.class);

        bindPresenter(ExperimentsOverviewPresenter.class,
                ExperimentsOverviewPresenter.MyView.class,
                ExperimentsOverviewView.class,
                ExperimentsOverviewPresenter.MyProxy.class);

        bindPresenter(ExperimentDetailPresenter.class,
                ExperimentDetailPresenter.MyView.class,
                ExperimentDetailView.class,
                ExperimentDetailPresenter.MyProxy.class);


        bindPresenter(ExperimentsOverviewTabPresenter.class,
                ExperimentsOverviewTabPresenter.MyView.class,
                ExperimentsOverviewTabView.class,
                ExperimentsOverviewTabPresenter.MyProxy.class);

        bindPresenter(ExperimentDetailTabPresenter.class,
                ExperimentDetailTabPresenter.MyView.class,
                ExperimentDetailTabView.class,
                ExperimentDetailTabPresenter.MyProxy.class);

        bindPresenter(PhenotypeListPresenter.class,
                PhenotypeListPresenter.MyView.class, PhenotypeListView.class,
                PhenotypeListPresenter.MyProxy.class);

        bindPresenter(PhenotypeDetailTabPresenter.class,
                PhenotypeDetailTabPresenter.MyView.class,
                PhenotypeDetailTabView.class,
                PhenotypeDetailTabPresenter.MyProxy.class);

        bindPresenter(PhenotypeDetailPresenter.class,
                PhenotypeDetailPresenter.MyView.class,
                PhenotypeDetailView.class,
                PhenotypeDetailPresenter.MyProxy.class);

        bindPresenter(StudyListPresenter.class,
                StudyListPresenter.MyView.class, StudyListView.class,
                StudyListPresenter.MyProxy.class);

        bindPresenter(StudyTabPresenter.class, StudyTabPresenter.MyView.class,
                StudyTabView.class, StudyTabPresenter.MyProxy.class);

        bindPresenter(StudyDetailPresenter.class,
                StudyDetailPresenter.MyView.class, StudyDetailView.class,
                StudyDetailPresenter.MyProxy.class);

        bindPresenter(StudyGWASPlotPresenter.class,
                StudyGWASPlotPresenter.MyView.class, StudyGWASPlotView.class,
                StudyGWASPlotPresenter.MyProxy.class);

        bindPresenterWidget(PermissionDetailPresenter.class,
                PermissionDetailPresenter.MyView.class,
                PermissionDetailView.class);


        bindPresenter(StudyWizardPresenter.class,
                StudyWizardPresenter.MyView.class, StudyWizardView.class,
                StudyWizardPresenter.MyProxy.class);

        bindPresenter(BasicStudyWizardPresenter.class,
                BasicStudyWizardPresenter.MyView.class, BasicStudyWizardView.class,
                BasicStudyWizardPresenter.MyProxy.class);


        bindPresenter(GermplasmPresenter.class,
                GermplasmPresenter.MyView.class, GermplasmView.class,
                GermplasmPresenter.MyProxy.class);

        bindPresenter(TaxonomyOverviewPresenter.class,
                TaxonomyOverviewPresenter.MyView.class,
                TaxonomyOverviewView.class,
                TaxonomyOverviewPresenter.MyProxy.class);

        bindPresenter(TaxonomyDetailPresenter.class,
                TaxonomyDetailPresenter.MyView.class, TaxonomyDetailView.class,
                TaxonomyDetailPresenter.MyProxy.class);

        bindPresenter(PassportListPresenter.class,
                PassportListPresenter.MyView.class, PassportListView.class,
                PassportListPresenter.MyProxy.class);

        bindPresenter(PassportDetailPresenter.class,
                PassportDetailPresenter.MyView.class, PassportDetailView.class,
                PassportDetailPresenter.MyProxy.class);

        bindPresenter(StockDetailPresenter.class,
                StockDetailPresenter.MyView.class, StockDetailView.class,
                StockDetailPresenter.MyProxy.class);

        bindPresenterWidget(SearchPresenter.class,
                SearchPresenter.MyView.class, SearchView.class);

        bindPresenter(PhenotypeOverviewPresenter.class,
                PhenotypeOverviewPresenter.MyView.class,
                PhenotypeOverviewView.class,
                PhenotypeOverviewPresenter.MyProxy.class);

        bindPresenter(StudyOverviewPresenter.class,
                StudyOverviewPresenter.MyView.class, StudyOverviewView.class,
                StudyOverviewPresenter.MyProxy.class);

        bindPresenter(TraitOntologyPresenter.class,
                TraitOntologyPresenter.MyView.class,
                TraitOntologyView.class,
                TraitOntologyPresenter.MyProxy.class);

        bindPresenter(PublicationOverviewPresenter.class,
                PublicationOverviewPresenter.MyView.class, PublicationOverviewView.class,
                PublicationOverviewPresenter.MyProxy.class);

        bindPresenter(PublicationDetailPresenter.class,
                PublicationDetailPresenter.MyView.class, PublicationDetailView.class,
                PublicationDetailPresenter.MyProxy.class);

        bindPresenter(MetaAnalysisGenePresenter.class,
                MetaAnalysisGenePresenter.MyView.class, MetaAnalysisGeneView.class,
                MetaAnalysisGenePresenter.MyProxy.class);

        bindPresenter(MetaAnalysisTopResultsPresenter.class,
                MetaAnalysisTopResultsPresenter.MyView.class, MetaAnalysisTopResultsView.class,
                MetaAnalysisTopResultsPresenter.MyProxy.class);

        bindPresenter(GenomeBrowserPresenter.class,
                GenomeBrowserPresenter.MyView.class, GenomeBrowserView.class,
                GenomeBrowserPresenter.MyProxy.class);

        bindPresenter(HomeTabPresenter.class, HomeTabPresenter.MyView.class, HomeTabView.class, HomeTabPresenter.MyProxy.class);

        bindPresenter(ProfilePresenter.class, ProfilePresenter.MyView.class, ProfileView.class, ProfilePresenter.MyProxy.class);

        bindPresenter(UserListPresenter.class,
                UserListPresenter.MyView.class, UserListView.class,
                UserListPresenter.MyProxy.class);

        bindPresenter(GWASViewerPresenter.class, GWASViewerPresenter.MyView.class, GWASViewerView.class, GWASViewerPresenter.MyProxy.class);

        bindPresenter(SNPDetailPresenter.class, SNPDetailPresenter.MyView.class, SNPDetailView.class, SNPDetailPresenter.MyProxy.class);

        bindPresenter(CandidateGeneListPresenter.class, CandidateGeneListPresenter.MyView.class, CandidateGeneListView.class, CandidateGeneListPresenter.MyProxy.class);
        bindPresenter(CandidateGeneListDetailPresenter.class, CandidateGeneListDetailPresenter.MyView.class, CandidateGeneListDetailView.class, CandidateGeneListDetailPresenter.MyProxy.class);
        bindPresenter(ExperimentCandidateGeneListEnrichmentPresenter.class, ExperimentCandidateGeneListEnrichmentPresenter.MyView.class, CandidateGeneListEnrichmentView.class, ExperimentCandidateGeneListEnrichmentPresenter.MyProxy.class);
        //Workaround because CandidateGeneListEnrichmentView was already bound
        bind(PhenotypeCandidateGeneListEnrichmentPresenter.class).in(Singleton.class);
        bind(PhenotypeCandidateGeneListEnrichmentPresenter.MyProxy.class).asEagerSingleton();
        //Workaround because CandidateGeneStudyCandidateGeneListEnrichmentPresenterListEnrichmentView was already bound
        bind(StudyCandidateGeneListEnrichmentPresenter.class).in(Singleton.class);
        bind(StudyCandidateGeneListEnrichmentPresenter.MyProxy.class).asEagerSingleton();


        bindPresenterWidget(GWASPlotPresenterWidget.class, GWASPlotPresenterWidget.MyView.class, GWASPlotView.class);
        bindPresenterWidget(FilterPresenterWidget.class, FilterPresenterWidget.MyView.class, FilterPresenterWidgetView.class);
        bindPresenterWidget(TextBoxFilterItemPresenterWidget.class, TextBoxFilterItemPresenterWidget.MyView.class, TextBoxFilterItemPresenterWidgetView.class);
        bindPresenterWidget(DropDownFilterItemPresenterWidget.class, DropDownFilterItemPresenterWidget.MyView.class, DropDownFilterItemPresenterWidgetView.class);
        bindPresenterWidget(TypeaheadFilterItemPresenterWidget.class, TypeaheadFilterItemPresenterWidget.MyView.class, TypeaheadFilterItemPresenterWidgetView.class);
        bindPresenterWidget(FacetSearchPresenterWidget.class, FacetSearchPresenterWidget.MyView.class, FacetSearchPresenterWidgetView.class);
        bindPresenterWidget(PhenotypeUploadWizardPresenterWidget.class, PhenotypeUploadWizardPresenterWidget.MyView.class, PhenotypeUploadWizardView.class);
        bindSingletonPresenterWidget(GWASUploadWizardPresenterWidget.class, GWASUploadWizardPresenterWidget.MyView.class, GWASUploadWizardView.class);
        //bindPresenterWidget(CandidateGeneListEnrichmentPresenterWidget.class,CandidateGeneListEnrichmentPresenterWidget.MyView.class,CandidateGeneListEnrichmentPresenterWidgetView.class);
        // have to use that otherwise @Assited throws error
        //bind(CandidateGeneListEnrichmentPresenterWidget.MyView.class).to(CandidateGeneListEnrichmentPresenterWidgetView.class);
    }
}
