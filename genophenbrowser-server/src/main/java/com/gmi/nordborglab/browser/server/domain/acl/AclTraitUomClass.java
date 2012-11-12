package com.gmi.nordborglab.browser.server.domain.acl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom")
public class AclTraitUomClass extends AclClass {
	
	@OneToMany(mappedBy="aclClass",cascade={CascadeType.PERSIST,CascadeType.MERGE})
    private Set<AclTraitUomIdentity> objectIdentities = new HashSet<AclTraitUomIdentity>();
	
	public Set<AclTraitUomIdentity> getExperimentIdentities() {
		return objectIdentities;
	}

}
