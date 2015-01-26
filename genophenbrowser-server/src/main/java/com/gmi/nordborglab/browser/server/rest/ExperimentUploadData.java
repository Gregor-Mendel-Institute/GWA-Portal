package com.gmi.nordborglab.browser.server.rest;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import org.hibernate.validator.constraints.NotEmpty;
import org.isatools.isacreator.model.Contact;
import org.isatools.isacreator.model.Investigation;
import org.springframework.util.comparator.BooleanComparator;

import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by uemit.seren on 6/3/14.
 */
public class ExperimentUploadData {

    final static DateFormat dateFormatter = new SimpleDateFormat("dd/MM/YYYY");

    @NotNull
    protected Experiment experiment;

    @NotNull
    @NotEmpty
    protected List<PhenotypeUploadData> phenotypes;

    protected List<Publication> publications;
    protected String name;
    protected String description;
    protected String doi;
    protected Date created;
    protected Date published;
    protected String originator;

    @NotEmpty
    @NotNull
    protected List<SampleData> sampleData;

    private String errorMessage;


    public ExperimentUploadData() {
    }


    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public List<PhenotypeUploadData> getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(List<PhenotypeUploadData> phenotypes) {
        this.phenotypes = phenotypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public static ExperimentUploadData createFromInvestigation(Investigation inv) {
        ExperimentUploadData data = new ExperimentUploadData();
        data.description = inv.getInvestigationDescription();
        data.name = String.format("%s - %s", inv.getInvestigationId(), inv.getInvestigationTitle());
        try {
            if (inv.getSubmissionDate() != null) {
                data.created = dateFormatter.parse(inv.getSubmissionDate());
            }
            if (inv.getPublicReleaseDate() != null) {
                data.published = dateFormatter.parse(inv.getPublicReleaseDate());
            }
        } catch (Exception e) {

        }
        if (inv.getContacts() != null && inv.getContacts().size() > 0) {
            Contact contact = inv.getContacts().get(0);
            data.originator = String.format("%s %s", contact.getFirstName(), contact.getLastName());
        }
        return data;
    }

    public List<SampleData> getSampleData() {
        return sampleData;
    }

    public void setSampleData(List<SampleData> sampleData) {
        this.sampleData = sampleData;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void sortByErrors() {
        Collections.sort(sampleData, new Comparator<SampleData>() {
            @Override
            public int compare(SampleData o1, SampleData o2) {
                return BooleanComparator.TRUE_LOW.compare(o1.hasError(), o2.hasError());
            }
        });
    }


}
