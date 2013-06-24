package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;

import javax.persistence.*;

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
@SequenceGenerator(name = "idSequence", sequenceName = "util.gwas_results_id_seq")
public class GWASResult extends SecureEntity {

    private String name;

    private String type;
    private String comments;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private AppUser appUser;
    private float maxScore;
    private int numberOfSNPs;

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

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
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
}
