package com.gmi.nordborglab.jpaontology.model;

import javax.persistence.*;

@Entity
@Table(name="term_definition")
public class TermDefinition {

    @Id
    private Integer term_id;
	
	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "term_id")
	private Term term;
	
	@Column(name="term_definition")
	private String termDefinition;
	
	@Column(name="term_comment")
	private String termComment;
	
	private String reference;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "dbxref_id")
	private DBXref dbxref;

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public String getTermDefinition() {
		return termDefinition;
	}

	public void setTermDefinition(String termDefinition) {
		this.termDefinition = termDefinition;
	}

	public String getTermComment() {
		return termComment;
	}

	public void setTermComment(String termComment) {
		this.termComment = termComment;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public DBXref getDbxref() {
		return dbxref;
	}

	public void setDbxref(DBXref dbxref) {
		this.dbxref = dbxref;
	}
	
	
	
}
