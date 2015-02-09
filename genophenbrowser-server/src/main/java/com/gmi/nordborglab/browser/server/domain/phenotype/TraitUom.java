package com.gmi.nordborglab.browser.server.domain.phenotype;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.google.common.collect.Iterables;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "div_trait_uom", schema = "phenotype")
@AttributeOverride(name = "id", column = @Column(name = "div_trait_uom_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "phenotype.div_trait_uom_div_trait_uom_id_seq", allocationSize = 1)
public class TraitUom extends SecureEntity {

    public static final String ES_TYPE = "phenotype";

    @ManyToOne()
    @JoinColumn(name = "div_unit_of_measure_id")
    private UnitOfMeasure unitOfMeasure;

    @OneToMany(mappedBy = "traitUom", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<Trait> traits = new HashSet<Trait>();

    @NotNull
    @Column(name = "local_trait_name")
    private String localTraitName;
    private String trait_protocol;
    @Column(name = "to_accession")
    private String toAccession;
    @Column(name = "eo_accession")
    private String eoAccession;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modified = new Date();
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();
    @Temporal(TemporalType.TIMESTAMP)
    private Date published;

    @Transient
    private Experiment experiment;

    @Transient
    private Long numberOfObsUnits;

    @Transient
    private Long numberOfStudies;

    @Transient
    private Term traitOntologyTerm;

    @Transient
    private Term environOntologyTerm;

    @Transient
    private List<StatisticType> statisticTypes;

    @Transient
    private List<Long> statisticTypeTraitCounts;

    public TraitUom() {
    }


    public Experiment getExperiment() {
        ///TODO change database schema for more efficient access
        if (experiment == null) {
            Trait trait = Iterables.get(traits, 0);
            experiment = trait.getObsUnit().getExperiment();
        }
        return experiment;
    }

    public Set<Trait> getTraits() {
        return traits;
    }

    public List<StatisticType> getStatisticTypes() {
        return statisticTypes;
    }

    public void setStatisticTypes(List<StatisticType> statisticTypes) {
        this.statisticTypes = statisticTypes;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getLocalTraitName() {
        return localTraitName;
    }

    public void setLocalTraitName(String localTraitName) {
        this.localTraitName = localTraitName;
    }

    public String getTraitProtocol() {
        return trait_protocol;
    }

    public void setTraitProtocol(String traitProtocol) {
        this.trait_protocol = traitProtocol;
    }

    public String getToAccession() {
        return toAccession;
    }

    public void setToAccession(String toAccession) {
        this.toAccession = toAccession;
    }

    public String getEoAccession() {
        return eoAccession;
    }

    public void setEoAccession(String eoAccession) {
        this.eoAccession = eoAccession;
    }

    public Long getNumberOfObsUnits() {
        return numberOfObsUnits;
    }

    public void setNumberOfObsUnits(Long count) {
        this.numberOfObsUnits = count;
    }

    public Long getNumberOfStudies() {
        return numberOfStudies;
    }

    public void setNumberOfStudies(Long numberOfStudies) {
        this.numberOfStudies = numberOfStudies;
    }

    public Term getTraitOntologyTerm() {
        return traitOntologyTerm;
    }

    public void setTraitOntologyTerm(Term traitOntologyTerm) {
        this.traitOntologyTerm = traitOntologyTerm;
        if (traitOntologyTerm == null) {
            toAccession = null;
        } else {
            toAccession = traitOntologyTerm.getAcc();
        }
    }

    public void addTrait(Trait trait) {
        traits.add(trait);
        trait.setTraitUom(this);
    }


    public void setStatisticTypeTraitCounts(List<Long> statisticTypeTraitCounts) {
        this.statisticTypeTraitCounts = statisticTypeTraitCounts;
    }

    public List<Long> getStatisticTypeTraitCounts() {
        return statisticTypeTraitCounts;
    }

    public Date getModified() {
        return modified;
    }

    public Date getCreated() {
        return created;
    }

    public Date getPublished() {
        return published;
    }

    public Term getEnvironOntologyTerm() {
        return environOntologyTerm;
    }

    public void setEnvironOntologyTerm(Term environOntologyTerm) {
        this.environOntologyTerm = environOntologyTerm;
        if (environOntologyTerm == null) {
            eoAccession = null;
        } else {
            eoAccession = environOntologyTerm.getAcc();
        }
    }

    @PreRemove
    public void preRemove() {
        unitOfMeasure = null;
        statisticTypes = null;
        experiment = null;
    }

    @Override
    public XContentBuilder getXContent(XContentBuilder builder) throws IOException {
        if (builder == null)
            builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("local_trait_name", this.getLocalTraitName())
                .field("published", this.getPublished())
                .field("modified", this.getModified())
                .field("created", this.getCreated())
                .field("trait_protocol", this.getTraitProtocol())
                .startObject("experiment")
                .field("name", this.getExperiment().getName())
                .field("id", this.getExperiment().getId())
                .endObject();
        if (this.getTraitOntologyTerm() != null) {
            builder.startObject("to_accession");
            addOntologyToBuilder(builder, this.getTraitOntologyTerm());
            builder.endObject();
        }
        if (this.getEnvironOntologyTerm() != null) {
            builder.startObject("eo_accession");
            addOntologyToBuilder(builder, this.getEnvironOntologyTerm());
            builder.endObject();
        }
        return builder;
    }

    private static void addOntologyToBuilder(XContentBuilder builder, Term term) throws IOException {
        builder.field("term_id", term.getAcc())
                .field("term_definition", term.getTermDefinition().getTermDefinition())
                .field("term_comment", term.getTermDefinition().getTermComment())
                .field("term_name", term.getName());
    }

    @Override
    public String getEsType() {
        return ES_TYPE;
    }

    @Override
    public String getRouting() {
        return getExperiment().getId().toString();
    }

    @Override
    public String getParentId() {
        return experiment.getId().toString();
    }
}
