package com.gmi.nordborglab.browser.server.domain.germplasm;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

@Entity
@Table(name="div_generation",schema="germplasm")
@AttributeOverride(name="id", column=@Column(name="div_generation_id"))
@SequenceGenerator(name="idSequence", sequenceName="germplasm.div_generation_div_generation_id_seq")
public class Generation extends BaseEntity{
	
	private String icisId;
	private String comments;
	private Integer selfingNumber;
	private Integer sibbingNumber;
	
	public Generation() {
		
	}


	public String getIcisId() {
		return icisId;
	}


	public void setIcisId(String icisId) {
		this.icisId = icisId;
	}


	public String getComments() {
		return comments;
	}


	public void setComments(String comments) {
		this.comments = comments;
	}


	public Integer getSelfingNumber() {
		return selfingNumber;
	}


	public void setSelfingNumber(Integer selfingNumber) {
		this.selfingNumber = selfingNumber;
	}


	public Integer getSibbingNumber() {
		return sibbingNumber;
	}


	public void setSibbingNumber(Integer sibbingNumber) {
		this.sibbingNumber = sibbingNumber;
	}
	
	

}
