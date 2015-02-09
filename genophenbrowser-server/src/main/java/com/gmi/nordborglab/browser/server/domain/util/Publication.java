package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.es.ESDocument;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.IOException;
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
@Table(name = "publications", schema = "util")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "util.publications_id_seq")
public class Publication extends BaseEntity implements ESDocument {

    private String doi;
    private String volume;
    private String journal;
    private String title;
    private String url;
    private String issue;
    private String author;
    private String page;

    public static final String ES_TYPE = "publication";

    private Date pubdate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "publications")
    private Set<Experiment> experiments = new HashSet<Experiment>();

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

    public Date getCreated() {
        return created;
    }

    @Override
    public XContentBuilder getXContent(XContentBuilder builder) throws IOException {
        if (builder == null)
            builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("journal", this.getJournal())
                .field("author", this.getFirstAuthor())
                .field("title", this.getTitle())
                .field("page", this.getPage())
                .field("pubdate", this.getPubDate())
                .field("issue", this.getIssue())
                .field("volune", this.getVolume())
                .field("url", this.getURL())
                .field("doi", this.getURL())
                .endObject();
        return builder;
    }

    @Override
    public String getEsType() {
        return ES_TYPE;
    }

    @Override
    public String getEsId() {
        if (getId() != null)
            return getId().toString();
        return null;
    }

    @Override
    public String getRouting() {
        return null;
    }

    @Override
    public String getParentId() {
        return null;
    }

}
