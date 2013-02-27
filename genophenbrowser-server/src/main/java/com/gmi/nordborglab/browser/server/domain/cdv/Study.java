package com.gmi.nordborglab.browser.server.domain.cdv;

import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.google.common.collect.Iterables;

@Entity
@Table(name = "cdv_g2p_study", schema = "cdv")
@AttributeOverride(name = "id", column = @Column(name = "cdv_g2p_study_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "cdv.cdv_g2p_study_cdv_g2p_study_id_seq")
public class Study extends BaseEntity {

	@ManyToMany(fetch = FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.MERGE},mappedBy="studies")
	private Set<Trait> traits  = new HashSet<Trait>();
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="cdv_g2p_protocol_id")
	private StudyProtocol protocol;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_allele_assay_id")
	private AlleleAssay alleleAssay;

    @ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "cdv_phen_transformation_id",nullable = false)
    private Transformation transformation;

	private String name;
	private String producer;

    @Temporal(TemporalType.TIMESTAMP)
    private Date study_date = new Date();
	@NotNull
	private Boolean isdone=false;
	
	@Transient
	private TraitUom phenotype;
	
	
	@Transient
	private CustomAccessControlEntry userPermission = new CustomAccessControlEntry(0L, 0, true);
	
	@Transient 
	boolean isOwner = false;
	
	
	
	public Set<Trait> getTraits() {
		return Collections.unmodifiableSet(traits);
	}
	
	public TraitUom getPhenotype() {
		if (phenotype == null) {
			if (getTraits().size() > 0)
				phenotype = Iterables.get(getTraits(),0).getTraitUom();
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

	public Boolean isDone() {
		return isdone;
	}

	public void setIsDone(Boolean isDone) {
		this.isdone = isDone;
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
		this.alleleAssay = alleleAssay;
		alleleAssay.addStudy(this);
	}
	
	public void addTraits(Set<Trait> traits) {
		this.traits.addAll(traits);
		for (Trait trait: traits) {
			trait.addStudy(this);
		}
	}
	public void addTrait(Trait trait) {
		traits.add(trait);
		trait.addStudy(this);
	}
	
	public void setTraits(Set<Trait> traits) {
		for (Trait trait: traits) {
			trait.addStudy(this);
		}
		this.traits = traits;
	}
	
	public CustomAccessControlEntry getUserPermission() {
		return userPermission;
	}
	
	public void setUserPermission(CustomAccessControlEntry userPermission)  {
		this.userPermission = userPermission;
	}
	
	public void setIsOwner(boolean isOwner) {
		this.isOwner = isOwner;
	}
	public boolean isOwner() {
		return isOwner;
	}



    public Transformation getTransformation() {
        return transformation;
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
        transformation.getStudies().add(this);
    }
}
