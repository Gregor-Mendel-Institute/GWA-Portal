package com.gmi.nordborglab.browser.server.domain.germplasm;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

@Entity
@Table(name="div_stock_parent",schema="germplasm")
@AttributeOverride(name="id", column=@Column(name="div_stock_parent_id"))
@SequenceGenerator(name="idSequence", sequenceName="germplasm.div_stock_parent_div_stock_parent_id_seq")
public class StockParent extends BaseEntity{

	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "div_parent_id")
	private Stock parent; 
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name = "div_stock_id")
	private Stock child;
	
	private String role;
	private Integer recurrent;
	
	
	public Stock getParent() {
		return parent;
	}
	public void setParent(Stock parent) {
		this.parent = parent;
	}
	public Stock getChild() {
		return child;
	}
	public void setChild(Stock child) {
		this.child = child;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Integer getRecurrent() {
		return recurrent;
	}
	public void setRecurrent(Integer recurrent) {
		this.recurrent = recurrent;
	}
}
