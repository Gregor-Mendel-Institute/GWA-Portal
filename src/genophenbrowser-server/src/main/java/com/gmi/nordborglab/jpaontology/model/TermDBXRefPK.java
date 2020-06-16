package com.gmi.nordborglab.jpaontology.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class TermDBXRefPK implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name="term_id")
	private Term term;
	
	@ManyToOne
	@JoinColumn(name="dbxref_id")
	private DBXref dbxref;
	
	private Boolean is_for_definition;

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public DBXref getDbxref() {
		return dbxref;
	}

	public void setDbxref(DBXref dbxref) {
		this.dbxref = dbxref;
	}

	public Boolean getIs_for_definition() {
		return is_for_definition;
	}

	public void setIs_for_definition(Boolean is_for_definition) {
		this.is_for_definition = is_for_definition;
	}
	
}
