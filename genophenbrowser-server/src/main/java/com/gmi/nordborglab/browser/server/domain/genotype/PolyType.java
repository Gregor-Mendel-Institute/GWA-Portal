package com.gmi.nordborglab.browser.server.domain.genotype;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

@Entity 
@Table(name="div_poly_type", schema="genotype")
@AttributeOverride(name="id", column=@Column(name="div_poly_type_id"))
@SequenceGenerator(name="idSequence", sequenceName="genotype.div_poly_type_div_poly_type_id_seq")
public class PolyType extends BaseEntity {

	private String poly_type;

	public String getPolyType() {
		return poly_type;
	}

	public void setPolyType(String poly_type) {
		this.poly_type = poly_type;
	}
	
	
}
