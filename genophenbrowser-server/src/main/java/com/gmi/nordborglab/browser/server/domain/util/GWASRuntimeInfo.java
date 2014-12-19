package com.gmi.nordborglab.browser.server.domain.util;

/**
 * Created by uemit.seren on 12/18/14.
 */

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "gwas_runtime_info", schema = "util")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@SequenceGenerator(name = "idSequence", sequenceName = "util.gwas_runtime_info_id_seq", allocationSize = 1)
public class GWASRuntimeInfo extends BaseEntity {


    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "div_allele_assay_id")
    protected AlleleAssay alleleAssay;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "cdv_g2p_protocol_id")
    protected StudyProtocol studyProtocol;

    protected Double coefficient1;
    protected Double coefficient2;
    protected Double coefficient3;

    public GWASRuntimeInfo() {
    }

    public AlleleAssay getAlleleAssay() {
        return alleleAssay;
    }

    public StudyProtocol getStudyProtocol() {
        return studyProtocol;
    }

    public Long getAlleleAssayId() {
        return alleleAssay.getId();
    }

    public Long getStudyProtocolId() {
        return studyProtocol.getId();
    }

    public Double getCoefficient1() {
        return coefficient1;
    }

    public Double getCoefficient2() {
        return coefficient2;
    }

    public Double getCoefficient3() {
        return coefficient3;
    }
}
