package com.gmi.nordborglab.browser.server.domain.observation;

import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AclExperimentIdentity;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;

@Entity
@Table(name="div_experiment",schema="observation")
@AttributeOverride(name="id", column=@Column(name="div_experiment_id"))
@SequenceGenerator(name="idSequence", sequenceName="observation.div_experiment_div_experiment_id_seq")
public class Experiment extends BaseEntity {

	private String name;
	private String design;
	private String originator;
	private String comments;
	
	@OneToOne(mappedBy="experiment",cascade={CascadeType.PERSIST,CascadeType.MERGE})
    private AclExperimentIdentity acl;
	
	@OneToMany(mappedBy="experiment",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<ObsUnit> obsUnits;
	
	@Transient
	private CustomAccessControlEntry userPermission = null;
	
	@Transient 
	boolean isOwner = false;
	
	@Transient
	int numberOfPhenotypes = 0; 
	
	public Experiment() { }
	
	
	public AclExperimentIdentity getAcl() {
		return acl;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesign() {
		return design;
	}
	public void setDesign(String design) {
		this.design = design;
	}
	public String getOriginator() {
		return originator;
	}
	public void setOriginator(String originator) {
		this.originator = originator;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}

	public Set<ObsUnit> getObsUnits() {
		return obsUnits;
	}

	public void setPhenotypes(Set<ObsUnit> obsUnits) {
		this.obsUnits = obsUnits;
	}
	
	public CustomAccessControlEntry getUserPermission() {
		return userPermission;
	}
	
	public void setUserPermission(CustomAccessControlEntry userPermission)  {
		this.userPermission = userPermission;
	}
	
	public void setIsOwner(boolean isOwner) {
		this.isOwner = isOwner;
	}
	public boolean isOwner() {
		return isOwner;
	}


	public int getNumberOfPhenotypes() {
		return numberOfPhenotypes;
	}


	public void setNumberOfPhenotypes(int numberOfPhenotypes) {
		this.numberOfPhenotypes = numberOfPhenotypes;
	}
	
	
}
