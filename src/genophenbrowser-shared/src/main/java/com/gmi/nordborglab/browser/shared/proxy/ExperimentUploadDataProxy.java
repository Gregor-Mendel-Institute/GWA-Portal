package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Created by uemit.seren on 6/27/14.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.rest.ExperimentUploadData")
public interface ExperimentUploadDataProxy extends ValueProxy {

    @NotNull
    public ExperimentProxy getExperiment();

    public void setExperiment(ExperimentProxy experiment);

    @NotNull
    public List<PhenotypeUploadDataProxy> getPhenotypes();

    public void setPhenotypes(List<PhenotypeUploadDataProxy> phenotypes);

    public String getName();

    public void setName(String name);

    public String getDescription();

    public void setDescription(String description);

    public String getDoi();

    public void setDoi(String doi);

    public Date getCreated();

    public void setCreated(Date created);

    public String getOriginator();

    public void setOriginator(String originator);

    public Date getPublished();

    public void setPublished(Date published);

    @NotNull
    public List<SampleDataProxy> getSampleData();

    public String getErrorMessage();

    void setSampleData(List<SampleDataProxy> samples);
}
