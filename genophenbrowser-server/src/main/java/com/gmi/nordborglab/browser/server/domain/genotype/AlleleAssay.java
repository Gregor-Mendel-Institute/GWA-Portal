package com.gmi.nordborglab.browser.server.domain.genotype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;

@Entity 
@Table(name="div_allele_assay", schema="genotype")
@AttributeOverride(name="id", column=@Column(name="div_allele_assay_id"))
@SequenceGenerator(name="idSequence", sequenceName="genotype.div_allele_assay_div_allele_assay_id_seq")
public class AlleleAssay extends BaseEntity {

	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_scoring_tech_type_id")
	private ScoringTechType scoringTechType;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_poly_type_id")
	private PolyType polyType;
	
	@OneToMany(mappedBy="alleleAssay",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private List<Allele> alleles = new ArrayList<Allele>();
	
	@OneToMany(mappedBy="alleleAssay",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private List<Study> studies = new ArrayList<Study>();
	
	private String name;
	private String producer;
	private String comments;
	private Date assay_date;
	
	
	public ScoringTechType getScoringTechType() {
		return scoringTechType;
	}
	public void setScoringTechType(ScoringTechType scoringTechType) {
		this.scoringTechType = scoringTechType;
	}
	public PolyType getPolyType() {
		return polyType;
	}
	public void setPolyType(PolyType polyType) {
		this.polyType = polyType;
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
	public void setProducer(String producer) {
		this.producer = producer;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Date getAssayDate() {
		return assay_date;
	}
	public void setAssayDate(Date assay_date) {
		this.assay_date = assay_date;
	}
	public List<Allele> getAlleles() {
		return Collections.unmodifiableList(alleles);
	}
	public void addStudy(Study study) {
		studies.add(study);
	}
	
	public List<Study> getStudies() {
		return Collections.unmodifiableList(studies);
	}
}
