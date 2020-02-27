package com.gmi.nordborglab.browser.server.domain.germplasm;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.es.ESDocument;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "div_stock", schema = "germplasm")
@AttributeOverride(name = "id", column = @Column(name = "div_stock_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "germplasm.div_stock_div_stock_id_seq", allocationSize = 1)
public class Stock extends BaseEntity implements ESDocument {

    public static final String ES_TYPE = "stock";

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "div_generation_id")
    private Generation generation;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "div_passport_id")
    private Passport passport;

    @OneToMany(mappedBy = "stock", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ObsUnit> obsUnits = new ArrayList<ObsUnit>();

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<StockParent> parents = new HashSet<StockParent>();

    @OneToMany(mappedBy = "child", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<StockParent> childs = new HashSet<StockParent>();

    @Column(name = "seed_lot")
    private String seedLot;
    @Column(name = "stock_source")
    private String stockSource;
    private String comments;

    @Transient
    private String pedigreeData;

    public Stock() {

    }

    public Generation getGeneration() {
        return generation;
    }

    public void setGeneration(Generation generation) {
        this.generation = generation;
    }

    public Passport getPassport() {
        return passport;
    }

    public void setPassport(Passport passport) {
        this.passport = passport;
    }

    public String getSeedLot() {
        return seedLot;
    }

    public void setSeedLot(String seedLot) {
        this.seedLot = seedLot;
    }

    public String getStockSource() {
        return stockSource;
    }

    public void setStockSource(String stockSource) {
        this.stockSource = stockSource;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Set<StockParent> getParents() {
        return parents;
    }


    public Set<StockParent> getChilds() {
        return childs;
    }

    public List<ObsUnit> getObsUnits() {
        return obsUnits;
    }


    public String getPedigreeData() {
        return pedigreeData;
    }

    public void setPedigreeData(String pedigreeData) {
        this.pedigreeData = pedigreeData;
    }

    @Override
    public XContentBuilder getXContent(XContentBuilder builder) throws IOException {
        if (builder == null)
            builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("seed_lot", getSeedLot())
                .field("div_passport_id", getPassport().getId())
                .field("div_taxonomy_id", getPassport().getTaxonomy().getId())
                .field("comment", getComments())
                .field("stock_source", getStockSource());
        if (getGeneration() != null) {
            builder.startObject("generation")
                    .field("icis_id", getGeneration().getIcisId())
                    .field("selfing_number", getGeneration().getSelfingNumber())
                    .field("sibbing_number", getGeneration().getSibbingNumber())
                    .field("comments", getGeneration().getComments())
                    .endObject();
        }
        return builder;
    }

    @Override
    public String getEsType() {
        return ES_TYPE;
    }

    @Override
    public String getEsId() {
        if (getId() != null)
            return getId().toString();
        return null;
    }

    @Override
    public String getRouting() {
        return getPassport().getTaxonomy().getId().toString();
    }

    @Override
    public String getParentId() {
        return getPassport().getId().toString();
    }
}
