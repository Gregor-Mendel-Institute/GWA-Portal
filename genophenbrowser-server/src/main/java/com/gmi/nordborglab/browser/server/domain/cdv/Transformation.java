package com.gmi.nordborglab.browser.server.domain.cdv;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 1:48 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "cdv_phen_transformation", schema = "cdv")
@AttributeOverride(name = "id", column = @Column(name = "cdv_phen_transformation_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "cdv.cdv_phen_transformation_cdv_phen_transformation_id_seq", allocationSize = 1)
public class Transformation extends BaseEntity {

    public Transformation() {
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "transformation")
    private Set<Study> studies = new HashSet<Study>();

    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Study> getStudies() {
        return studies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
