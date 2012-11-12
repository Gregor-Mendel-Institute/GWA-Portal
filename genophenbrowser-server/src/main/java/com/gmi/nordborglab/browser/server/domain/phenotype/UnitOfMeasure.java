package com.gmi.nordborglab.browser.server.domain.phenotype;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

@Entity
@Table(name="div_unit_of_measure",schema="phenotype")
@AttributeOverride(name="id", column=@Column(name="div_unit_of_measure_id"))
@SequenceGenerator(name="idSequence", sequenceName="phenotype.div_unit_of_measure_div_unit_of_measure_id_seq")
public class UnitOfMeasure extends BaseEntity {
	
	private String unit_type;
	
	public UnitOfMeasure() { }

	public String getUnitType() {
		return unit_type;
	}

	public void setUnitType(String unitType) {
		this.unit_type = unitType;
	}
	
	

}
