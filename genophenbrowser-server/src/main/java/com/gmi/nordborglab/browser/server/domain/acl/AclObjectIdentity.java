package com.gmi.nordborglab.browser.server.domain.acl;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;



@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="object_id_class", discriminatorType=DiscriminatorType.INTEGER)
@Table(name="acl_object_identity", schema="acl")
@SequenceGenerator(name="idSequence", sequenceName="acl.acl_object_identity_id_seq")
public class AclObjectIdentity extends BaseEntity{
	
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="owner_sid")
	private AclSid owner;
	
	

	public AclSid getOwner() {
		return owner;
	}
}
