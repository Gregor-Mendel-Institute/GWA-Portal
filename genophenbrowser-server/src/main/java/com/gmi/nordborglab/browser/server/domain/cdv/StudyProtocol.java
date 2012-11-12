package com.gmi.nordborglab.browser.server.domain.cdv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

@Entity 
@Table(name="cdv_g2p_protocol", schema="cdv")
@AttributeOverride(name="id", column=@Column(name="cdv_g2p_protocol_id"))
@SequenceGenerator(name="idSequence", sequenceName="cdv.cdv_source_cdv_source_id_seq")
public class StudyProtocol extends BaseEntity {
	
	private String analysis_method;
	
	@OneToMany(mappedBy="protocol",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private List<Study> studies = new ArrayList<Study>();
	
	public String getAnalysisMethod() {
		return analysis_method;
	}
	
	public void setAnalysisMethod(String analysis_method) {
		this.analysis_method = analysis_method;
	}
	
	protected void addStudy(Study study) {
		studies.add(study);
	}
	
	public List<Study> getStudies() {
		return Collections.unmodifiableList(studies);
	}

}
