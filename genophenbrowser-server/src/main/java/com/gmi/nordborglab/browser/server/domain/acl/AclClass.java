package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="class", discriminatorType=DiscriminatorType.STRING)
@Table(name="acl_class", schema="acl")
public class AclClass {
	
	@Id
	@Column(unique=true,nullable=false)
	private Long id;
	

	public Long getId() {
		return id;
	}

}
