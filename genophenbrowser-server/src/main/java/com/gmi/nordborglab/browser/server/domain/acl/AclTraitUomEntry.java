package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="acl_entry", schema="acl")
public class AclTraitUomEntry extends AclEntry{
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="acl_object_identity")
	private AclTraitUomIdentity objectIdentity;
	
	
	public AclTraitUomIdentity getObjectIdentity() {
		return objectIdentity;
	}
}
