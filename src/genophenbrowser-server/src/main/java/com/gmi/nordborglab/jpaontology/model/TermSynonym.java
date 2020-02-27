package com.gmi.nordborglab.jpaontology.model;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;



@Entity
@Table(name="term_synonym")
public class TermSynonym  {
	
	@EmbeddedId
	private TermSynonymPK termSynonymId;

	
	private String acc_synonym;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "synonym_type_id")
	private Term synonymType;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "synonym_category_id")
	private Term synonymCategory;

	public String getAcc_synonym() {
		return acc_synonym;
	}

	public void setAcc_synonym(String acc_synonym) {
		this.acc_synonym = acc_synonym;
	}

	public Term getSynonymType() {
		return synonymType;
	}

	public void setSynonymType(Term synonymType) {
		this.synonymType = synonymType;
	}

	public Term getSynonymCategory() {
		return synonymCategory;
	}

	public void setSynonymCategory(Term synonymCategory) {
		this.synonymCategory = synonymCategory;
	}
}
