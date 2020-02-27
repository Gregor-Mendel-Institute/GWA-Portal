package com.gmi.nordborglab.jpaontology.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="term_dbxref")
public class TermDBXref  {
	
	@EmbeddedId
	private TermDBXRefPK termDBXRefId;

	public TermDBXRefPK getTermDBXRefId() {
		return termDBXRefId;
	}

	public void setTermDBXRefId(TermDBXRefPK termDBXRefId) {
		this.termDBXRefId = termDBXRefId;
	}
	

}
