package com.gmi.nordborglab.browser.server.domain.util;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GoTerm;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "candidateGeneList", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<CandidateGeneListEnrichment> candidateGeneListEnrichments = new ArrayList<CandidateGeneListEnrichment>();

    @Transient
    private List<Gene> genesWithInfo = new ArrayList<Gene>();

    @ElementCollection()
    @CollectionTable(name = "candidate_gene_list_genes", schema = "util", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "gene")
    private Set<String> genes = new HashSet<String>();

    public static final String ES_TYPE = "candidate_gene_list";


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

    @Transient
    public int getEnrichmentCount() {
        return candidateGeneListEnrichments != null ? candidateGeneListEnrichments.size() : 0;
    }


    public List<CandidateGeneListEnrichment> getCandidateGeneListEnrichments() {
        return candidateGeneListEnrichments;
    }

    @Override
    public XContentBuilder getXContent(XContentBuilder builder) throws IOException {
        if (builder == null)
            builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("name", this.getName())
                .field("published", this.getPublished())
                .field("description", this.getDescription())
                .field("modified", this.getModified())
                .field("created", this.getCreated());
        if (this.getGenesWithInfo() != null && this.getGenesWithInfo().size() > 0) {
            builder.startArray("genes");
            for (Gene gene : this.getGenesWithInfo()) {
                addGeneToBuilder(builder, gene);
            }
            builder.endArray();
        }
        return builder;
    }

    private static void addGeneToBuilder(XContentBuilder builder, Gene gene) throws IOException {
        builder.startObject()
                .field("name", gene.getName())
                .field("symbol", gene.getSymbol())
                .field("synonyms", gene.getSynonyms())
                .field("chr", gene.getChr())
                .field("start_pos", gene.getStart())
                .field("end_pos", gene.getEnd())
                .field("strand", gene.getStrand())
                .field("description", gene.getDescription())
                .field("short_description", gene.getShortDescription())
                .field("curator_summary", gene.getCuratorSummary())
                .field("annotation", gene.getAnnotation());
        if (gene.getGoTerms() != null && gene.getGoTerms().size() > 0) {
            builder.startArray("GO");
            for (GoTerm term : gene.getGoTerms()) {
                addGoTermToBuilder(builder, term);
            }
            builder.endArray();
        }
        builder.endObject();
    }

    private static void addGoTermToBuilder(XContentBuilder builder, GoTerm term) throws IOException {
        builder.startObject()
                .field("relation", term.getRelation())
                .field("exact", term.getExact())
                .field("narrow", term.getNarrow())
                .endObject();
    }

    @Override
    public String getEsType() {
        return ES_TYPE;
    }

    @Override
    public String getRouting() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getParentId() {
        return null;
    }
}
