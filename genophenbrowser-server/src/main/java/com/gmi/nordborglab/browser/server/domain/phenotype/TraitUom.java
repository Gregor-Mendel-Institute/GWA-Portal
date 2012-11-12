package com.gmi.nordborglab.browser.server.domain.phenotype;

import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AclTraitUomIdentity;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;

@Entity
@Table(name="div_trait_uom",schema="phenotype")
@AttributeOverride(name="id", column=@Column(name="div_trait_uom_id"))
@SequenceGenerator(name="idSequence", sequenceName="phenotype.div_trait_uom_div_trait_uom_id_seq")
public class TraitUom extends BaseEntity {
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_unit_of_measure_id")
	private UnitOfMeasure unitOfMeasure;
	
	@OneToOne(mappedBy="traitUom",cascade={CascadeType.PERSIST,CascadeType.MERGE})
    private AclTraitUomIdentity acl;
	
	@OneToMany(mappedBy="traitUom",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<Trait> traits;
	
	private String local_trait_name;
	private String trait_protocol;
	private String to_accession;
	private String eo_accession;
	
	@Transient
	private Long numberOfObsUnits;
	
	@Transient
	private Long numberOfStudies;
	
	@Transient
	private CustomAccessControlEntry userPermission = null;
	@Transient 
	boolean isOwner = false;
	
	@Transient 
	private Set<StatisticType> statisticTypes;
	
	public TraitUom() { }
	
	public AclTraitUomIdentity getAcl() {
		return acl;
	}
	
	public Set<Trait> getTraits() {
		return traits;
	}
	
	public Set<StatisticType> getStatisticTypes() {
		return statisticTypes;
	}
	
	public void setStatisticTypes(Set<StatisticType> statisticTypes) {
		this.statisticTypes = statisticTypes;
	}
	
	public UnitOfMeasure getUnitOfMeasure() {
		return unitOfMeasure;
	}
	public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}
	public String getLocalTraitName() {
		return local_trait_name;
	}
	public void setLocalTraitName(String localTraitName) {
		this.local_trait_name = localTraitName;
	}
	public String getTraitProtocol() {
		return trait_protocol;
	}
	public void setTraitProtocol(String traitProtocol) {
		this.trait_protocol = traitProtocol;
	}
	public String getToAccession() {
		return to_accession;
	}
	public void setToAccession(String toAccession) {
		this.to_accession = toAccession;
	}
	public String getEoAccession() {
		return eo_accession;
	}
	public void setEoAccession(String eoAccession) {
		this.eo_accession = eoAccession;
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

	public Long getNumberOfObsUnits() {
		return numberOfObsUnits;
	}

	public void setNumberOfObsUnits(Long count) {
		this.numberOfObsUnits = count;
	}

	public Long getNumberOfStudies() {
		return numberOfStudies;
	}

	public void setNumberOfStudies(Long numberOfStudies) {
		this.numberOfStudies = numberOfStudies;
	}
}
