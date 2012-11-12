package com.gmi.nordborglab.browser.server.domain.acl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("com.gmi.nordborglab.browser.server.domain.observation.Experiment")
public class AclExperimentClass extends AclClass{

	@OneToMany(mappedBy="aclClass",cascade={CascadeType.PERSIST,CascadeType.MERGE})
    private Set<AclExperimentIdentity> objectIdentities = new HashSet<AclExperimentIdentity>();
	
	public Set<AclExperimentIdentity> getExperimentIdentities() {
		return objectIdentities;
	}
	
}
