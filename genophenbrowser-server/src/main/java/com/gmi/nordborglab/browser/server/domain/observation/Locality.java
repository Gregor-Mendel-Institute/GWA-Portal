package com.gmi.nordborglab.browser.server.domain.observation;

import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.germplasm.AccessionCollection;

@Entity
@Table(name="div_locality",schema="observation")
@AttributeOverride(name="id", column=@Column(name="div_locality_id"))
@SequenceGenerator(name="idSequence", sequenceName="observation.div_locality_div_locality_id_seq")
public class Locality  extends BaseEntity{

	private Integer elevation;
	private String city;
	private String country;
	private String origcty;
	private Double latitude;
	private Double longitude;
	private String localityName;
	private String stateProvince;
	private String loAccession;
	
	@OneToMany(mappedBy="locality",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<AccessionCollection> accessionCollections;
	
	public Locality() {
		
	}

	public Set<AccessionCollection> getAccessionCollections() {
		return accessionCollections;
	}
	public Integer getElevation() {
		return elevation;
	}

	public void setElevation(Integer elevation) {
		this.elevation = elevation;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getOrigcty() {
		return origcty;
	}

	public void setOrigcty(String origcty) {
		this.origcty = origcty;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getLocalityName() {
		return localityName;
	}

	public void setLocalityName(String localityName) {
		this.localityName = localityName;
	}

	public String getStateProvince() {
		return stateProvince;
	}

	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	public String getLoAccession() {
		return loAccession;
	}

	public void setLoAccession(String loAccession) {
		this.loAccession = loAccession;
	}
	
	
}
