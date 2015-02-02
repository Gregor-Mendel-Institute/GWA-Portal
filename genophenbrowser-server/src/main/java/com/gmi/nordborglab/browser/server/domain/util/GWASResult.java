package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.es.ESDocument;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.IOException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "gwas_results", schema = "util")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "util.gwas_results_id_seq", allocationSize = 1)
public class GWASResult extends SecureEntity implements ESDocument {

    private String name;

    private String type;
    private String comments;

    private float maxScore;
    private int numberOfSNPs;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();
    @Temporal(TemporalType.TIMESTAMP)
    private Date published;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified = new Date();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public void setNumberOfSNPs(int numberOfSNPs) {
        this.numberOfSNPs = numberOfSNPs;
    }

    public int getNumberOfSNPs() {
        return numberOfSNPs;
    }

    @Override
    public String getIndexType() {
        return "gwasviewer";
    }

    @Override
    public String getRouting() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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


    @Override
    public XContentBuilder getXContent() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("name", this.getName())
                .field("type", this.getType())
                .field("comments", this.getComments())
                .field("maxscore", this.getMaxScore())
                .field("numberofsnps", this.getNumberOfSNPs())
                .field("created", this.getCreated())
                .field("published", this.getPublished())
                .field("modified", this.getModified());
        return builder;
    }

    @Override
    public String getEsType() {
        return "gwasviewer";
    }

    @Override
    public String getEsId() {
        if (getId() != null)
            return getId().toString();
        return null;
    }
}
