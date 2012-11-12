package com.gmi.nordborglab.browser.server.domain.germplasm;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.observation.Locality;

@Entity
@Table(name="div_accession_collecting",schema="germplasm")
@AttributeOverride(name="id", column=@Column(name="div_accession_collecting_id"))
@SequenceGenerator(name="idSequence", sequenceName="germplasm.div_accession_collecting_div_accession_collecting_id_seq")
public class AccessionCollection extends BaseEntity{

	@ManyToOne
	@JoinColumn(name="div_locality_id")
	private Locality locality;
	private String collector;
	private String collNumb;
	private String collSrc;
	private String collCode;
	private String collDate;
	
	public AccessionCollection() {
		
	}

	public Locality getLocality() {
		return locality;
	}

	public void setLocality(Locality locality) {
		this.locality = locality;
	}

	public String getCollector() {
		return collector;
	}

	public void setCollector(String collector) {
		this.collector = collector;
	}

	public String getCollNumb() {
		return collNumb;
	}

	public void setCollNumb(String collNumb) {
		this.collNumb = collNumb;
	}

	public String getCollSrc() {
		return collSrc;
	}

	public void setCollSrc(String collSrc) {
		this.collSrc = collSrc;
	}

	public String getCollCode() {
		return collCode;
	}

	public void setCollCode(String collCode) {
		this.collCode = collCode;
	}

	public String getCollDate() {
		return collDate;
	}

	public void setCollDate(String collDate) {
		this.collDate = collDate;
	}
	
}
