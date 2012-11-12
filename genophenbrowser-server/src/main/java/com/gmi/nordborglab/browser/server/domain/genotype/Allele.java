package com.gmi.nordborglab.browser.server.domain.genotype;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;

@Entity 
@Table(name="div_allele", schema="genotype")
@AttributeOverride(name="id", column=@Column(name="div_allele_id"))
@SequenceGenerator(name="idSequence", sequenceName="genotype.div_allele_div_allele_id_seq")
public class Allele extends BaseEntity {
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_allele_assay_id")
	private AlleleAssay alleleAssay;
	
	@ManyToOne(optional=true,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_passport_id")
	private Passport passport;
	
	private Integer allele_num;
	private String accession;
	private String referencedb;
	
	public AlleleAssay getAlleleAssay() {
		return alleleAssay;
	}
	public void setAlleleAssay(AlleleAssay alleleAssay) {
		this.alleleAssay = alleleAssay;
	}
	public Integer getAlleleNum() {
		return allele_num;
	}
	public void setAlleleNum(Integer allele_num) {
		this.allele_num = allele_num;
	}
	public String getAccession() {
		return accession;
	}
	public void setAccession(String accession) {
		this.accession = accession;
	}
	public String getReferencedb() {
		return referencedb;
	}
	public void setReferencedb(String referencedb) {
		this.referencedb = referencedb;
	}
	
	public void setPassport(Passport passport) {
		this.passport = passport;
	}
	public Passport getPassport() {
		return passport;
	}
}
