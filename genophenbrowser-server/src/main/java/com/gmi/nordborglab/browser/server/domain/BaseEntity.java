package com.gmi.nordborglab.browser.server.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;


@MappedSuperclass
public class BaseEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="idSequence")
	@Column(unique=true,nullable=false)
	private Long id;
	
	@Transient
	private Integer version = 0;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}

}
