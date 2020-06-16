package com.gmi.nordborglab.browser.server.domain.cdv;

import com.fasterxml.jackson.annotation.JsonView;
import com.gmi.nordborglab.browser.server.controller.rest.json.Views;
import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.google.common.collect.Iterables;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cdv_g2p_study", schema = "cdv")
@AttributeOverride(name = "id", column = @Column(name = "cdv_g2p_study_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "cdv.cdv_g2p_study_cdv_g2p_study_id_seq", allocationSize = 1)
public class Study extends SecureEntity {

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "studies")
    private Set<Trait> traits = new HashSet<Trait>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "cdv_g2p_protocol_id")
    @JsonView(Views.StudyDetail.class)
    private StudyProtocol protocol;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "div_allele_assay_id")
    @JsonView(Views.Studies.class)
    private AlleleAssay alleleAssay;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "cdv_phen_transformation_id", nullable = false)
    @JsonView(Views.StudyDetail.class)
    private Transformation transformation;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, mappedBy = "study", orphanRemoval = true)
    @JsonView(Views.StudyDetail.class)
    private StudyJob job;

    @JsonView(Views.Public.class)
    private String name;
    private String producer;

    @Column(name = "pseudo_heritability")
    private Double pseudoHeritability;

    @Column(name = "shapiro_wilk_pvalue")
    private Double shapiroWilkPvalue;

    @Temporal(TemporalType.TIMESTAMP)
    private Date study_date = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @JsonView(Views.Public.class)
    private Date created = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @JsonView(Views.Public.class)
    private Date published;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonView(Views.Public.class)
    private Date modified = new Date();


    @Transient
    private TraitUom phenotype;

    @Transient
    private boolean createEnrichments = false;

    @OneToMany(mappedBy = "study", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<CandidateGeneListEnrichment> candidateGeneListEnrichments = new ArrayList<CandidateGeneListEnrichment>();

    public static final String ES_TYPE = "study";


    public Set<Trait> getTraits() {
        return Collections.unmodifiableSet(traits);
    }

    public TraitUom getPhenotype() {
        if (phenotype == null) {
            if (getTraits().size() > 0)
                phenotype = Iterables.get(getTraits(), 0).getTraitUom();
        }
        return phenotype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producter) {
        this.producer = producter;
    }

    public Date getStudyDate() {
        return study_date;
    }

    public void setStudyDate(Date study_date) {
        this.study_date = study_date;
    }

    public StudyProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(StudyProtocol protocol) {
        this.protocol = protocol;
        protocol.addStudy(this);
    }

    public AlleleAssay getAlleleAssay() {
        return alleleAssay;
    }

    public void setAlleleAssay(AlleleAssay alleleAssay) {
        if (this.alleleAssay != alleleAssay) {
            this.pseudoHeritability = null;
        }
        this.alleleAssay = alleleAssay;
        alleleAssay.addStudy(this);
    }

    public void addTraits(Set<Trait> traits) {
        this.traits.addAll(traits);
        for (Trait trait : traits) {
            trait.addStudy(this);
        }
    }

    public void addTrait(Trait trait) {
        traits.add(trait);
        trait.addStudy(this);
    }

    public void setTraits(Set<Trait> traits) {
        for (Trait trait : traits) {
            trait.addStudy(this);
        }
        this.traits = traits;
    }


    public Transformation getTransformation() {
        return transformation;
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
        transformation.getStudies().add(this);
    }

    public StudyJob getJob() {
        return job;
    }

    public void setJob(StudyJob job) {
        this.job = job;
        job.setStudy(this);
    }

    public void removeJob() {
        this.job = null;
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
    public XContentBuilder getXContent(XContentBuilder builder) throws IOException {
        if (builder == null)
            builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("name", this.getName())
                .field("published", this.getPublished())
                .field("modified", this.getModified())
                .field("created", this.getCreated())
                .field("producer", this.getProducer())
                .field("study_date", this.getStudyDate())
                .startObject("phenotype")
                .field("name", this.getPhenotype().getLocalTraitName())
                .field("id", this.getPhenotype().getId())
                .endObject()
                .startObject("experiment")
                .field("name", this.getPhenotype().getExperiment().getName())
                .field("id", this.getPhenotype().getExperiment().getId())
                .endObject();

        if (this.getProtocol() != null) {
            builder.startObject("protocol")
                    .field("analysis_method", this.getProtocol().getAnalysisMethod()).endObject();
        }

        if (this.getAlleleAssay() != null) {
            builder.startObject("genotype");
            this.getAlleleAssay().getXContent(builder);
            builder.endObject();
        }
        return builder;
    }

    @Override
    public String getEsType() {
        return ES_TYPE;
    }

    @Override
    public String getRouting() {
        return getPhenotype().getExperiment().getId().toString();
    }

    @Override
    public String getParentId() {
        return getPhenotype().getId().toString();
    }

    public List<CandidateGeneListEnrichment> getCandidateGeneListEnrichments() {
        return candidateGeneListEnrichments;
    }

    public void setCandidateGeneListEnrichments(List<CandidateGeneListEnrichment> candidateGeneListEnrichments) {
        this.candidateGeneListEnrichments = candidateGeneListEnrichments;
    }

    public void setCreateEnrichments(boolean createEnrichments) {
        this.createEnrichments = createEnrichments;
    }

    public boolean isCreateEnrichments() {
        return createEnrichments;
    }

    public Double getPseudoHeritability() {
        return pseudoHeritability;
    }

    public void setPseudoHeritability(Double pseudoHeritability) {
        this.pseudoHeritability = pseudoHeritability;
    }

    public Double getShapiroWilkPvalue() {
        return shapiroWilkPvalue;
    }

    public void setShapiroWilkPvalue(Double shapiroWilkPvalue) {
        this.shapiroWilkPvalue = shapiroWilkPvalue;
    }
}
