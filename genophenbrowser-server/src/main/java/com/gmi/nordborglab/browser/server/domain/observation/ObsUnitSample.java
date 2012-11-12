package com.gmi.nordborglab.browser.server.domain.observation;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

@Entity
@Table(name="div_obs_unit_sample",schema="observation")
@AttributeOverride(name="id", column=@Column(name="div_obs_unit_sample_id"))
@SequenceGenerator(name="idSequence", sequenceName="observation.div_obs_unit_sample_div_obs_unit_sample_id_seq")
public class ObsUnitSample extends BaseEntity {
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="div_obs_unit_id")
	private ObsUnit obsUnit;
	
	private String name;
	@Temporal(TemporalType.TIMESTAMP)
	private Date sample_date;
	private String producer;
	private String comments;
	
	public ObsUnitSample() {}
	
	public ObsUnit getObsUnit() {
		return obsUnit;
	}
	public void setObsUnit(ObsUnit obsUnit) {
		this.obsUnit = obsUnit;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getSampleDate() {
		return sample_date;
	}
	public void setSampleDate(Date sample_date) {
		this.sample_date = sample_date;
	}
	public String getProducer() {
		return producer;
	}
	public void setProducer(String producer) {
		this.producer = producer;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	

}
