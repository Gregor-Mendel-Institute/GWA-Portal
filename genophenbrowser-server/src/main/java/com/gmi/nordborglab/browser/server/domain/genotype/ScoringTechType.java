package com.gmi.nordborglab.browser.server.domain.genotype;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

@Entity 
@Table(name="div_scoring_tech_type", schema="genotype")
@AttributeOverride(name="id", column=@Column(name="div_scoring_tech_type_id"))
@SequenceGenerator(name="idSequence", sequenceName="genotype.div_scoring_tech_type_div_scoring_tech_type_id_seq")
public class ScoringTechType extends BaseEntity {

	private String scoring_tech_group;
	private String scoring_tech_type;
	
	public String getScoringTechGroup() {
		return scoring_tech_group;
	}
	public void setScoringTechGroup(String scoring_tech_group) {
		this.scoring_tech_group = scoring_tech_group;
	}
	public String getScoringTechType() {
		return scoring_tech_type;
	}
	public void setScoringTechType(String scoring_tech_type) {
		this.scoring_tech_type = scoring_tech_type;
	}
}
