package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampleDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.common.collect.Lists;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.requestfactory.shared.RequestContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by uemit.seren on 6/29/14.
 */
public class AutoBeanCloneUtils {

    public static PhenotypeUploadDataProxy clonePhenotypeUploadData(AutoBean<PhenotypeUploadDataProxy> bean, RequestContext ctx, List<UnitOfMeasureProxy> unitOfMeasures) {
        PhenotypeUploadDataProxy data = ctx.create(PhenotypeUploadDataProxy.class);
        AutoBean<PhenotypeUploadDataProxy> newBean = AutoBeanUtils.getAutoBean(data);
        AutoBeanCodex.decodeInto(AutoBeanCodex.encode(bean), newBean);
        data = newBean.as();
        data.setTraitUom(ctx.create(PhenotypeProxy.class));
        data.getTraitUom().setLocalTraitName(data.getName());
        data.getTraitUom().setTraitProtocol(data.getProtocol());
        data.getTraitUom().setUnitOfMeasure(getUnitOfMeasureFromName(data.getUnitOfMeasure(), unitOfMeasures));
        if (data.getTraitOntology() != null) {
            //data.getTraitUom().setTraitOntologyTerm(data.getTraitOntology());
        }
        if (data.getEnvironmentOntology() != null) {
            //data.getTraitUom().setEnvironOntologyTerm(data.getEnvironmentOntology());
        }
        return data;
    }


    public static SampleDataProxy cloneSampleData(AutoBean<SampleDataProxy> bean, RequestContext ctx) {
        AutoBean<SampleDataProxy> newSampleBean = AutoBeanUtils.getAutoBean(ctx.create(SampleDataProxy.class));
        AutoBeanCodex.decodeInto(AutoBeanCodex.encode(bean), newSampleBean);
        return newSampleBean.as();
    }

    public static ExperimentUploadDataProxy cloneExperimentUploadData(AutoBean<ExperimentUploadDataProxy> bean, ExperimentProxy experiment, RequestContext ctx, List<UnitOfMeasureProxy> unitOfMeasures) {
        ExperimentUploadDataProxy data = ctx.create(ExperimentUploadDataProxy.class);
        AutoBean<ExperimentUploadDataProxy> newBean = AutoBeanUtils.getAutoBean(data);
        AutoBeanCodex.decodeInto(AutoBeanCodex.encode(bean), newBean);
        data = newBean.as();
        List<PhenotypeUploadDataProxy> phenotypes = new ArrayList<PhenotypeUploadDataProxy>();
        for (PhenotypeUploadDataProxy phenotype : bean.as().getPhenotypes()) {
            AutoBean<PhenotypeUploadDataProxy> phen = AutoBeanUtils.getAutoBean(phenotype);
            phenotypes.add(clonePhenotypeUploadData(phen, ctx, unitOfMeasures));
        }
        List<SampleDataProxy> samples = Lists.newArrayList();
        for (SampleDataProxy sample : bean.as().getSampleData()) {
            AutoBean<SampleDataProxy> sampleBean = AutoBeanUtils.getAutoBean(sample);
            samples.add(cloneSampleData(sampleBean, ctx));
        }

        if (experiment != null) {
            data.setExperiment(experiment);
        } else {
            AutoBean<ExperimentProxy> expBean = AutoBeanUtils.getAutoBean(ctx.create(ExperimentProxy.class));
            data.setExperiment(expBean.as());
            data.getExperiment().setName(data.getName());
            data.getExperiment().setDesign(data.getDescription());
            data.getExperiment().setOriginator(data.getOriginator());
        }
        data.setPhenotypes(phenotypes);
        data.setSampleData(samples);
        return data;
    }

    private static UnitOfMeasureProxy getUnitOfMeasureFromName(String name, List<UnitOfMeasureProxy> availableUnitOfMeasures) {
        if (name == null)
            return null;
        for (UnitOfMeasureProxy unitOfMeasure : availableUnitOfMeasures) {

            if (unitOfMeasure != null && unitOfMeasure.getUnitType().equalsIgnoreCase(name)) {
                return unitOfMeasure;
            }
        }
        return null;
    }


}
