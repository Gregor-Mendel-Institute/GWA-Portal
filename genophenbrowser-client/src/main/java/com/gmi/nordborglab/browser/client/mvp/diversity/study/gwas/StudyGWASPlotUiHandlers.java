package com.gmi.nordborglab.browser.client.mvp.diversity.study.gwas;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created by uemit.seren on 5/4/16.
 */
public interface StudyGWASPlotUiHandlers extends UiHandlers {
    void showLdForSNP(String chromosome, Integer position);

    void showExactLdForRegion(String chromosome, Integer position);

    void showLdForRegion(String chromosome, Integer position);
}
