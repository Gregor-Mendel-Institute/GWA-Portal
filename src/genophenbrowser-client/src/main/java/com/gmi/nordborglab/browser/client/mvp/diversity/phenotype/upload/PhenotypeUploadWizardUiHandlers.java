package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload;

import com.gmi.nordborglab.browser.shared.proxy.SampleDataProxy;
import com.google.common.collect.Multiset;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/8/13
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PhenotypeUploadWizardUiHandlers extends UiHandlers {

    void onUploadFinished(String responseText);

    void onUploadError(String responseText);

    void onCancel();

    void onCreate();

    void startUpload();

    void updatePhenotypeData();

    void deselectPhenotypeCard();

    Collection<SampleDataProxy> getExplorerData();

    Multiset<String> getGeoChartdata();

    Map<String, Double> getHistogramData();

    void updateTable();
}
