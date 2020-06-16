package com.gmi.nordborglab.browser.server.domain.germplasm;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "div_sampstat", schema = "germplasm")
@AttributeOverride(name = "id", column = @Column(name = "div_sampstat_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "germplasm.div_sampstat_div_sampstat_id_seq", allocationSize = 1)
public class Sampstat extends BaseEntity {

    private String sampstat;
    @Column(name = "germplasm_type")
    private String germplasmType;

    public Sampstat() {
    }

    public String getSampstat() {
        return sampstat;
    }

    public void setSampstat(String sampstat) {
        this.sampstat = sampstat;
    }

    public String getGermplasmType() {
        return germplasmType;
    }

    public void setGermplasmType(String germplasm_type) {
        this.germplasmType = germplasm_type;
    }
}
