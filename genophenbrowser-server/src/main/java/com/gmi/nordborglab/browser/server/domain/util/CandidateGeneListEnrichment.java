package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.12.13
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "candidate_gene_list_enrichment", schema = "util")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "util.candidate_gene_list_enrichment_id_seq", allocationSize = 1)
public class CandidateGeneListEnrichment extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "candidate_gene_list_id", nullable = false)
    private CandidateGeneList candidateGeneList;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "cdv_g2p_study_id", nullable = false)
    private Study study;

    private Double pvalue;

    private int windowsize = 20000;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date modified = new Date();

    private int progress = 0;

    private String task;

    private String taskid;

    private String payload;
    private int permutationcount = 10000;
    private int topsnpcount = 1000;


    public CandidateGeneListEnrichment() {
    }


    public CandidateGeneList getCandidateGeneList() {
        return candidateGeneList;
    }

    public void setCandidateGeneList(CandidateGeneList candidateGeneList) {
        this.candidateGeneList = candidateGeneList;
        candidateGeneList.getCandidateGeneListEnrichments().add(this);
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
        study.getCandidateGeneListEnrichments().add(this);
    }

    public Double getPvalue() {
        return pvalue;
    }

    public void setPvalue(Double pvalue) {
        this.pvalue = pvalue;
    }

    public int getWindowsize() {
        return windowsize;
    }

    public void setWindowsize(int windowsize) {
        this.windowsize = windowsize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }


    public void delete() {
        study = null;
        candidateGeneList = null;
    }

    public int getPermutationCount() {
        return permutationcount;
    }

    public int getTopSNPCount() {
        return topsnpcount;
    }

    public void setPermutationCount(int permutationcount) {
        this.permutationcount = permutationcount;
    }

    public void setTopSNPCount(int topsnpcount) {
        this.topsnpcount = topsnpcount;
    }
}
