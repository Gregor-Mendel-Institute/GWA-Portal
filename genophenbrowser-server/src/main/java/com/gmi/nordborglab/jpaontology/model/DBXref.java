package com.gmi.nordborglab.jpaontology.model;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="dbxref")
@SequenceGenerator(name="idSequence",sequenceName="dbxref_id_seq")
public class DBXref extends BaseOntologyEntity {
	
	private String xref_dbname;
	private String xref_key;
	private String xref_keytype;
	private String xref_desc;
	public String getXref_dbname() {
		return xref_dbname;
	}
	public void setXref_dbname(String xref_dbname) {
		this.xref_dbname = xref_dbname;
	}
	public String getXref_key() {
		return xref_key;
	}
	public void setXref_key(String xref_key) {
		this.xref_key = xref_key;
	}
	public String getXref_keytype() {
		return xref_keytype;
	}
	public void setXref_keytype(String xref_keytype) {
		this.xref_keytype = xref_keytype;
	}
	public String getXref_desc() {
		return xref_desc;
	}
	public void setXref_desc(String xref_desc) {
		this.xref_desc = xref_desc;
	}
		
}
