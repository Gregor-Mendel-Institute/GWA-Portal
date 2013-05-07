package com.gmi.nordborglab.browser.client;

public class NameTokens {

	public static final String home = "!home";
	public static final String experiments = "!studies";
	public static final String experiment = "!study/{id}/overview";
	public static final String phenotypes = "!study/{id}/phenotypes";
	public static final String phenotype = "!phenotype/{id}/overview";
	public static final String obsunit = "!phenotype/{id}/obsunits";
	public static final String studylist = "!phenotype/{id}/analysislist";
	public static final String study = "!analysis/{id}/overview";
	public static final String studygwas = "!analysis/{id}/studygwas";
	public static final String experimentPermission = "!experiment/{id}/permission";
	public static final String studywizard = "!phenotype/{id}/analysiswizard";
	public static final String taxonomies = "!taxonomies";
	public static final String taxonomy = "!taxonomy/{id}/overview";
	public static final String passports = "!taxonomy/{id}/passports";
	public static final String passport = "!passport/{id}/overview";
	public static final String stock = "!stock/{id}/overview";
	public static final String phenotypeoverview = "!phenotypes";
	public static final String studyoverview = "!analysisoverview";
	//TODO can't use {id} because of updateHistoryToken causes exception
    public static final String traitontology = "!ontology/{ontology}";
    public static final String gwasViewer="!gwasViewer";
    public static final String basicstudywizard ="!analysiswizard";
    public static final String publications = "!publications";
    public static final String publication ="!publication/{id}/overview";


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

	public static String getObsunit() {
		return obsunit;
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
}
