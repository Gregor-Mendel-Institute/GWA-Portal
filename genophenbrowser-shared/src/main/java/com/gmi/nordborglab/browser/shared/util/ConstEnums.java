package com.gmi.nordborglab.browser.shared.util;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.07.13
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public interface ConstEnums {

    enum ENRICHMENT_TYPE {STUDY, PHENOTYPE, ANALYSIS, CANDIDATE_GENE_LIST}

    enum TABLE_FILTER {ALL, PRIVATE, PUBLISHED, RECENT, SHARED}

    enum USER_FILTER {ALL, ADMIN, USER}

    enum GENE_FILTER {ALL, PROTEIN, PSEUDO, TRANSPOSON}

    enum ONTOLOGY_TYPE {TRAIT, ENVIRONMENT}

    enum FILTERS {STUDY, PHENOTYPE, METHOD, GENOTYPE, ANALYSIS, CANDIDATE_GENE_LIST, ID, PASSPORT_NAME, PASSPORT_COLLECTOR, COUNTRY, PASSPORT_TYPE}

    enum ENRICHMENT_FILTER {FINISHED, RUNNING, AVAILABLE}
}
