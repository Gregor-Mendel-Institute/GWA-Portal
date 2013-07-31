package com.gmi.nordborglab.browser.server.domain.observation;

import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.util.Publication;

@Entity
@Table(name = "div_experiment", schema = "observation")
@AttributeOverride(name = "id", column = @Column(name = "div_experiment_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "observation.div_experiment_div_experiment_id_seq")
public class Experiment extends SecureEntity {

    @NotNull
    @Size(min = 2)
    private String name;

    @NotNull
    @Size(min = 2)
    private String originator;

    private String design;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();
    @Temporal(TemporalType.TIMESTAMP)
    private Date published;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified = new Date();

    private String comments;

    @OneToMany(mappedBy = "experiment", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<ObsUnit> obsUnits = new HashSet<ObsUnit>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(schema = "util", name = "publications_experiment", inverseJoinColumns = @JoinColumn(name = "publication_id", referencedColumnName = "id"),
            joinColumns = @JoinColumn(name = "div_experiment_id", referencedColumnName = "div_experiment_id"))
    private Set<Publication> publications = new HashSet<Publication>();


    @Transient
    int numberOfPhenotypes = 0;
    @Transient
    private long numberOfAnalyses;

    @Transient
    private List<ESFacet> stats;

    public Experiment() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        this.design = design;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Set<ObsUnit> getObsUnits() {
        return obsUnits;
    }

    public void setPhenotypes(Set<ObsUnit> obsUnits) {
        this.obsUnits = obsUnits;
    }


    public int getNumberOfPhenotypes() {
        return numberOfPhenotypes;
    }


    public void setNumberOfPhenotypes(int numberOfPhenotypes) {
        this.numberOfPhenotypes = numberOfPhenotypes;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public void addPublication(Publication publication) {
        publications.add(publication);
        publication.addExperiment(this);
    }

    public void removePublication(Publication publication) {
        publications.remove(publication);
        publication.getExperiments().remove(this);
    }

    public Date getCreated() {
        return created;
    }

    public Date getPublished() {
        return published;
    }

    public Date getModified() {
        return modified;
    }

    public void setNumberOfAnalyses(long numberOfAnalyses) {
        this.numberOfAnalyses = numberOfAnalyses;
    }

    public long getNumberOfAnalyses() {
        return numberOfAnalyses;
    }

    public void setStats(List<ESFacet> stats) {
        this.stats = stats;
    }

    public List<ESFacet> getStats() {
        return stats;
    }
}
