package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.cdv.Transformation;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.TransformationData;
import com.gmi.nordborglab.browser.server.domain.stats.AppStat;
import com.gmi.nordborglab.browser.server.domain.stats.DateStatHistogramFacet;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.exceptions.CommandLineException;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;
import com.gmi.nordborglab.browser.shared.proxy.TransformationDataProxy;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.util.List;

public interface HelperService {

    List<BreadcrumbItem> getBreadcrumbs(Long id, String object);

    AppData getAppData();

    List<String> getGenesFromCanddiateGeneListUpload(byte[] inputStream) throws IOException;

    // TODO change to pass in TraitUom and statisticType for better performance
    List<TransformationData> calculateTransformations(List<Trait> values, Long alleleAssayId);

    @PreAuthorize("hasPermission(#traitUom,'READ')")
    Double getPseudoHeritability(Long alleleAssayId, TraitUom traitUom, Transformation transformation) throws CommandLineException;

    @PreAuthorize("#study.id == null or hasPermission(#situdy,'READ')")
    Double getPseudoHeritability(Study study) throws CommandLineException;

    @PreAuthorize("hasRole('ROLE_USER')")
    List<UserNotification> getUserNotifications(Integer limit);

    List<AppStat> getAppStats();

    List<DateStatHistogramFacet> findRecentTraitHistogram(DateStatHistogramProxy.INTERVAL interval);

    Study applyTransformation(Study study);

    ExperimentUploadData getExperimentUploadDataFromIsaTab(byte[] isaTabData);

    ExperimentUploadData getExperimentUploadDataFromCsv(byte[] csvData) throws IOException;

    SampleData parseAndUpdateAccession(SampleData value);

    Double calculatePseudoHeritability(List<Trait> traits, TransformationDataProxy.TYPE type, Long alleleAssayId) throws CommandLineException;

    @PreAuthorize("#study.id == null or hasPermission(#study,'READ')")
    Double calculateShapiroWilkPvalue(Study study);
}
