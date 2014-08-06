package com.gmi.nordborglab.browser.server.domain.phenotype;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.genotype.Allele;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@NamedEntityGraphs(
        @NamedEntityGraph(
                name = "statistics",
                attributeNodes = @NamedAttributeNode(value = "obsUnit", subgraph = "obsUnit"),
                subgraphs = {
                        @NamedSubgraph(name = "obsUnit", attributeNodes = @NamedAttributeNode(value = "stock", subgraph = "stock")),
                        @NamedSubgraph(name = "stock", attributeNodes = @NamedAttributeNode(value = "passport", subgraph = "passport")),
                        @NamedSubgraph(name = "passport", attributeNodes = {
                                @NamedAttributeNode(value = "collection", subgraph = "collection"),
                                @NamedAttributeNode(value = "alleles", subgraph = "alleles")
                        }),
                        @NamedSubgraph(name = "collection", attributeNodes = @NamedAttributeNode(value = "locality")),
                        @NamedSubgraph(name = "alleles", attributeNodes = @NamedAttributeNode(value = "alleleAssay"))
                }
        )
)
@Entity
@Table(name = "div_trait", schema = "phenotype")
@AttributeOverride(name = "id", column = @Column(name = "div_trait_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "phenotype.div_trait_div_trait_id_seq", allocationSize = 1)
public class Trait extends BaseEntity {

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(schema = "cdv", name = "cdv_pheno_set", inverseJoinColumns = @JoinColumn(name = "cdv_g2p_study_id", referencedColumnName = "cdv_g2p_study_id"),
            joinColumns = @JoinColumn(name = "div_trait_id", referencedColumnName = "div_trait_id"))
    private List<Study> studies = new ArrayList<Study>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "div_trait_uom_id")
    private TraitUom traitUom;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "div_statistic_type_id")
    private StatisticType statisticType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "div_obs_unit_id")
    private ObsUnit obsUnit;

    private String value;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date_measured;

    public Trait() {
    }

    public TraitUom getTraitUom() {
        return traitUom;
    }

    public void setTraitUom(TraitUom traitUom) {
        this.traitUom = traitUom;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public void setStatisticType(StatisticType statisticType) {
        this.statisticType = statisticType;
    }

    public ObsUnit getObsUnit() {
        return obsUnit;
    }

    public void setObsUnit(ObsUnit obsUnit) {
        this.obsUnit = obsUnit;
        obsUnit.getTraits().add(this);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDateMeasured() {
        return date_measured;
    }

    public void setDateMeasured(Date dateMeasured) {
        this.date_measured = dateMeasured;
    }

    public List<Study> getStudies() {
        return studies;
    }

    public void addStudy(Study study) {
        studies.add(study);
    }

    public Set<Allele> getAlleles() {
        return obsUnit.getStock().getPassport().getAlleles();
    }

}
