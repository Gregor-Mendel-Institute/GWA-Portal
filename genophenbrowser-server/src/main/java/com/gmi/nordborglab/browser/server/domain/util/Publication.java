package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/19/13
 * Time: 1:34 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name="publications",schema="util")
@AttributeOverride(name="id", column=@Column(name="id"))
@SequenceGenerator(name="idSequence", sequenceName="util.publications_id_seq")
public class Publication extends BaseEntity{

    private String doi;
    private String volume;
    private String journal;
    private String title;
    private String url;
    private String issue;
    private String author;
    private String page;
    private Date pubdate;

    @ManyToMany(fetch = FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.MERGE},mappedBy="publications")
    private Set<Experiment> experiments  = new HashSet<Experiment>();

    public String getDOI() {
        return doi;
    }

    public void setDOI(String doi) {
        this.doi = doi;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getFirstAuthor() {
        return author;
    }

    public void setFirstAuthor(String author) {
        this.author = author;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Date getPubDate() {
        return pubdate;
    }

    public void setPubDate(Date pubdate) {
        this.pubdate = pubdate;
    }

    public Set<Experiment> getExperiments() {
        return Collections.unmodifiableSet(experiments);
    }

    public void addExperiment(Experiment experiment) {
        experiments.add(experiment);
    }
}
