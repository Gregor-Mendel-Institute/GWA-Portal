package com.gmi.nordborglab.browser.server.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;

@Entity
@Table(name = "div_taxonomy", schema = "germplasm")
@AttributeOverride(name = "id", column = @Column(name = "div_taxonomy_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "germplasm.div_taxonomy_div_taxonomy_id_seq")
public class Taxonomy extends BaseEntity {

	@NotNull
	private String genus;
	
	@NotNull
	private String species;
	private String subspecies;
	private String subtaxa;
	private String race;
	private String population;
	
	@Column(name="common_name")
	private String commonName;
	@Column(name="term_accession")
	private String termAccession;
	
	@OneToMany(mappedBy = "taxonomy", cascade = { CascadeType.PERSIST,
			CascadeType.MERGE })
	private List<Passport> passports = new ArrayList<Passport>();

	@Transient
	private List<AlleleAssay> alleleAssays = new ArrayList<AlleleAssay>();
	
	public List<AlleleAssay> getAlleleAssays() {
		return alleleAssays;
	}

	public void setAlleleAssays(List<AlleleAssay> alleleAssays) {
		this.alleleAssays = alleleAssays;
	}

	

	public List<Passport> getPassports() {
		return passports;
	}

	public void setPassports(List<Passport> passports) {
		this.passports = passports;
	}

	public Taxonomy() {

	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getSubspecies() {
		return subspecies;
	}

	public void setSubspecies(String subspecies) {
		this.subspecies = subspecies;
	}

	public String getSubtaxa() {
		return subtaxa;
	}

	public void setSubtaxa(String subtaxa) {
		this.subtaxa = subtaxa;
	}

	public String getRace() {
		return race;
	}

	public void setRace(String race) {
		this.race = race;
	}

	public String getPopulation() {
		return population;
	}

	public void setPopulation(String population) {
		this.population = population;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getTermAccession() {
		return termAccession;
	}

	public void setTermAccession(String termAccession) {
		this.termAccession = termAccession;
	}
}
