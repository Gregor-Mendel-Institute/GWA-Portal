package com.gmi.nordborglab.browser.server.domain.acl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;

@Entity
@DiscriminatorValue("2")
public class AclTraitUomIdentity extends AclObjectIdentity {

	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="object_id_identity")
	private TraitUom traitUom;
	
	public TraitUom getTraitUom() {
		return traitUom;
	}
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="object_id_class",insertable=false,updatable=false)
	private AclTraitUomClass aclClass;

	@OneToMany(mappedBy="objectIdentity",cascade={CascadeType.PERSIST,CascadeType.MERGE})
    private Set<AclTraitUomEntry> aceEntries = new HashSet<AclTraitUomEntry>();
	
	public Set<AclTraitUomEntry> getEntries() {
		return aceEntries;
	}
	
	public AclTraitUomClass getAclClass() {
		return aclClass;
	}
}
