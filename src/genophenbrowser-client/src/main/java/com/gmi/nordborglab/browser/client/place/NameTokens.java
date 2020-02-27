package com.gmi.nordborglab.browser.client.place;

import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.client.GWT;

public final class NameTokens {

    public static final String home = "/home";
    public static final String experiments = "/studies";
    public static final String experiment = "/study/{id}/overview";
    public static final String phenotypes = "/study/{id}/phenotypes";
    public static final String phenotype = "/phenotype/{id}/overview";
    public static final String studylist = "/phenotype/{id}/analysislist";
    public static final String study = "/analysis/{id}/overview";
    public static final String studygwas = "/analysis/{id}/studygwas";
    public static final String experimentPermission = "/experiment/{id}/permission";
    public static final String studywizard = "/phenotype/{id}/analysiswizard";
    public static final String taxonomies = "/taxonomies";
    public static final String taxonomy = "/taxonomy/{id}/overview";
    public static final String passports = "/taxonomy/{id}/passports";
    public static final String passport = "/passport/{id}/overview";
    public static final String stock = "/stock/{id}/overview";
    public static final String phenotypeoverview = "/phenotypes";
    public static final String studyoverview = "/analysisoverview";
    //TODO can't use {id} because of updateHistoryToken causes exception
    public static final String traitontology = "/ontology/{ontology}";
    public static final String gwasViewer = "/gwasViewer";
    public static final String basicstudywizard = "/analysiswizard";
    public static final String publications = "/publications";
    public static final String publication = "/publication/{id}/overview";
    public static final String metaAnalysisGenes = "/meta/genes";
    public static final String metaAnalysisTopResults = "/meta/topresults";
    public static final String dashboard = "/dashboard";
    public static final String news = "/news/{id}";
    public static final String genomebrowser = "/genotype/genomebrowser";
    public static final String snpviewer = "/genotype/snpviewer";
    public static final String candidateGeneList = "/meta/candidategenelists";
    public static final String candidateGeneListDetail = "/meta/candidategenelist/{id}";
    public static final String profile = "/profile/{id}";
    public static final String account = "/account";
    public static final String userlist = "/users";
    public static final String experimentsEnrichments = "/study/{id}/enrichments";
    public static final String phenotypeEnrichments = "/phenotype/{id}/enrichments";
    public static final String studyEnrichments = "/analysis/{id}/enrichments";
    public static final String isaTabDownload = GWT.getHostPageBaseURL() + "/provider/study/{id}/{name}";

    public static final String snps = "/analysis/{id}/snp/{chr}/{position}";
    public static final ImmutableMap<SearchItemProxy.SUB_CATEGORY, String> subCategory2Token = ImmutableMap.<SearchItemProxy.SUB_CATEGORY, String>builder()
            .put(SearchItemProxy.SUB_CATEGORY.STUDY, NameTokens.experiments)
            .put(SearchItemProxy.SUB_CATEGORY.PHENOTYPE, NameTokens.phenotypeoverview)
            .put(SearchItemProxy.SUB_CATEGORY.ANALYSIS, NameTokens.studyoverview)
            .put(SearchItemProxy.SUB_CATEGORY.PUBLICATION, NameTokens.publications)
            .put(SearchItemProxy.SUB_CATEGORY.CANDIDATE_GENE_LIST, NameTokens.candidateGeneList)
            .build();


    public static String getHome() {
        return home;
    }


    public static String getExperiments() {
        return experiments;
    }

    public static String getExperiment() {
        return experiment;
    }

    public static String getPhenotypes() {
        return phenotypes;
    }

    public static String getPhenotype() {
        return phenotype;
    }

    public static String getStudylist() {
        return studylist;
    }

    public static String getStudy() {
        return study;
    }

    public static String getStudygwas() {
        return studygwas;
    }

    public static String getExperimentPermission() {
        return experimentPermission;
    }

    public static String getStudywizard() {
        return studywizard;
    }

    public static String getTaxonomies() {
        return taxonomies;
    }

    public static String getTaxonomy() {
        return taxonomy;
    }

    public static String getPassports() {
        return passports;
    }

    public static String getPassport() {
        return passport;
    }

    public static String getStock() {
        return stock;
    }

    public static String getPhenotypeoverview() {
        return phenotypeoverview;
    }

    public static String getStudyoverview() {
        return studyoverview;
    }

    public static String getTraitontology() {
        return traitontology;
    }

    public static String getBasicstudywizard() {
        return basicstudywizard;
    }


    public static String getGWASViewer() {
        return gwasViewer;
    }

    public static String getPublications() {
        return publications;
    }

    public static String getPublication() {
        return publication;
    }

    public static String getGwasViewer() {
        return gwasViewer;
    }

    public static String getMetaAnalysisGenes() {
        return metaAnalysisGenes;
    }

    public static String getMetaAnalysisTopResults() {
        return metaAnalysisTopResults;
    }

    public static String getGenomebrowser() {
        return genomebrowser;
    }

    public static String getCandidateGeneList() {
        return candidateGeneList;
    }

    public static String getCandidateGeneListDetail() {
        return candidateGeneListDetail;
    }

    public static String getProfile() {
        return profile;
    }

    public static String getAccount() {
        return account;
    }

    public static String getUserlist() {
        return userlist;
    }

    public static String getSNPs() {
        return snps;
    }

    public static String getSNPViewer() {
        return snpviewer;
    }
}
