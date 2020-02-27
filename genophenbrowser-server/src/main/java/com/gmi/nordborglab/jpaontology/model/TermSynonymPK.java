package com.gmi.nordborglab.jpaontology.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class TermSynonymPK implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String term_synonym;

	@ManyToOne
	@JoinColumn(name="term_id")
	private Term term;

	public String getTerm_synonym() {
		return term_synonym;
	}

	public void setTerm_synonym(String term_synony) {
		this.term_synonym = term_synony;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	} 
}
