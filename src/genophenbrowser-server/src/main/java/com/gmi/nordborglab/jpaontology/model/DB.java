package com.gmi.nordborglab.jpaontology.model;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="db")
@SequenceGenerator(name="idSequence",sequenceName="db_id_seq")
public class DB extends BaseOntologyEntity {

	private String name;
	
	private String fullname;
	private String datatype;
	private String generic_url;
	
	private String url_syntax;
	private String url_example;
	private String uri_prefix;
	
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public String getGeneric_url() {
		return generic_url;
	}
	public void setGeneric_url(String generic_url) {
		this.generic_url = generic_url;
	}
	public String getUrl_syntax() {
		return url_syntax;
	}
	public void setUrl_syntax(String url_syntax) {
		this.url_syntax = url_syntax;
	}
	public String getUrl_example() {
		return url_example;
	}
	public void setUrl_example(String url_example) {
		this.url_example = url_example;
	}
	public String getUri_prefix() {
		return uri_prefix;
	}
	public void setUri_prefix(String uri_prefix) {
		this.uri_prefix = uri_prefix;
	}
	
	
}
