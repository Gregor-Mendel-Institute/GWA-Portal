package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;

import javax.persistence.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 23.09.13
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "candidate_gene_list", schema = "util")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "util.candidate_gene_lists_id_seq", allocationSize = 1)
public class CandidateGeneList extends SecureEntity {

    private String name;
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();
    @Temporal(TemporalType.TIMESTAMP)
    private Date published;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified = new Date();


    @Transient
    private List<Gene> genesWithInfo = new ArrayList<Gene>();

    @ElementCollection()
    @CollectionTable(name = "candidate_gene_list_genes", schema = "util", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "gene")
    private Set<String> genes = new HashSet<String>();


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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Set<String> getGenes() {
        return genes;
    }

    public void setGenes(Set<String> genes) {
        this.genes = genes;
    }

    public List<Gene> getGenesWithInfo() {
        return genesWithInfo;
    }

    public void setGenesWithInfo(List<Gene> genesWithInfo) {
        this.genesWithInfo = genesWithInfo;
    }

    public int getGeneCount() {
        return genes != null ? genes.size() : 0;
    }
}
