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

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;

@Entity
@DiscriminatorValue("1")
public class AclExperimentIdentity extends AclObjectIdentity{

	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="object_id_identity")
	private Experiment experiment;
	
	public Experiment getExperiment() {
		return experiment;
	}
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="object_id_class",insertable=false,updatable=false)
	private AclExperimentClass aclClass;

	@OneToMany(mappedBy="objectIdentity",cascade={CascadeType.PERSIST,CascadeType.MERGE})
    private Set<AclExperimentEntry> aceEntries = new HashSet<AclExperimentEntry>();
	
	
	public Set<AclExperimentEntry> getEntries() {
		return aceEntries;
	}
	
	public AclExperimentClass getAclClass() {
		return aclClass;
	}
	
	
}
