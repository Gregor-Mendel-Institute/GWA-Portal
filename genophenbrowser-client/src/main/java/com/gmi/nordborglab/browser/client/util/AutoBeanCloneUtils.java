package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadValueProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
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
        List<PhenotypeUploadValueProxy> values = new ArrayList<PhenotypeUploadValueProxy>();
        for (PhenotypeUploadValueProxy value : bean.as().getPhenotypeUploadValues()) {
            AutoBean<PhenotypeUploadValueProxy> newValueBean = AutoBeanUtils.getAutoBean(ctx.create(PhenotypeUploadValueProxy.class));
            AutoBeanCodex.decodeInto(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(value)), newValueBean);
            values.add(newValueBean.as());
        }
        data.setTraitUom(ctx.create(PhenotypeProxy.class));
        data.getTraitUom().setLocalTraitName(data.getName());
        data.getTraitUom().setTraitProtocol(data.getProtocol());
        data.getTraitUom().setUnitOfMeasure(getUnitOfMeasureFromName(data.getUnitOfMeasure(), unitOfMeasures));
        if (data.getTraitOntology() != null) {
            data.getTraitUom().setTraitOntologyTerm(data.getTraitOntology());
        }
        if (data.getEnvironmentOntology() != null) {
            data.getTraitUom().setEnvironOntologyTerm(data.getEnvironmentOntology());
        }
        data.setPhenotypeUploadValues(values);
        return data;
    }


    public static ExperimentUploadDataProxy cloneExperimentUploadData(AutoBean<ExperimentUploadDataProxy> bean, RequestContext ctx, List<UnitOfMeasureProxy> unitOfMeasures) {
        ExperimentUploadDataProxy data = ctx.create(ExperimentUploadDataProxy.class);
        AutoBean<ExperimentUploadDataProxy> newBean = AutoBeanUtils.getAutoBean(data);
        AutoBeanCodex.decodeInto(AutoBeanCodex.encode(bean), newBean);
        data = newBean.as();
        List<PhenotypeUploadDataProxy> phenotypes = new ArrayList<PhenotypeUploadDataProxy>();
        for (PhenotypeUploadDataProxy phenotype : bean.as().getPhenotypes()) {
            AutoBean<PhenotypeUploadDataProxy> phen = AutoBeanUtils.getAutoBean(phenotype);
            phenotypes.add(clonePhenotypeUploadData(phen, ctx, unitOfMeasures));
        }
        data.setExperiment(ctx.create(ExperimentProxy.class));
        data.getExperiment().setName(data.getName());
        data.getExperiment().setDesign(data.getDescription());
        data.getExperiment().setOriginator(data.getOriginator());
        data.setPhenotypes(phenotypes);
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
