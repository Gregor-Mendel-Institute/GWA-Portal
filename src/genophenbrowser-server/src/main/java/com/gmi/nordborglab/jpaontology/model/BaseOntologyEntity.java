package com.gmi.nordborglab.jpaontology.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;


@MappedSuperclass
public class BaseOntologyEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="idSequence")
	@Column(unique=true,nullable=false)
	private Integer id;

	@Transient
	private Integer version = 0;
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
}