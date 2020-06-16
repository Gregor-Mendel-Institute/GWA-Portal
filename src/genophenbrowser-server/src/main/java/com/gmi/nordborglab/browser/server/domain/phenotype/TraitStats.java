package com.gmi.nordborglab.browser.server.domain.phenotype;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by uemit.seren on 8/4/14.
 */


@Entity
@Table(name = "view_phenotype_statistics", schema = "phenotype")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
public class TraitStats {

    @Id
    private Long div_passport_id;
    private String accename;
    private String origcty;
    private String country;
    private Double latitude;
    private Double longitude;
    private Double avg_value;
    @Column(name = "div_trait_uom_id")
    private Long traitUomId;

    @Column(name = "div_statistic_type_id")
    private Long statisticTypeId;


    public TraitStats() {
    }

    public Long getPassportId() {
        return div_passport_id;
    }

    public String getAccename() {
        return accename;
    }

    public String getOrigcty() {
        return origcty;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getAvgValue() {
        return avg_value;
    }

    public String getCountry() {
        return country;
    }

}
