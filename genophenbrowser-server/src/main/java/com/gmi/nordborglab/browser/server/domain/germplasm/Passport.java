package com.gmi.nordborglab.browser.server.domain.germplasm;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.cdv.Source;
import com.gmi.nordborglab.browser.server.domain.genotype.Allele;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.observation.Locality;
import com.gmi.nordborglab.browser.server.es.ESDocument;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.mysema.query.annotations.QueryInit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "div_passport", schema = "germplasm")
@AttributeOverride(name = "id", column = @Column(name = "div_passport_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "germplasm.div_passport_div_passport_id_seq", allocationSize = 1)
@BatchSize(size = 100)
public class Passport extends BaseEntity implements ESDocument {

    public static final String ES_TYPE = "passport";

    @ManyToOne()
    @JoinColumn(name = "div_taxonomy_id")
    private Taxonomy taxonomy;

    @ManyToOne()
    @JoinColumn(name = "div_accession_collecting_id")
    @QueryInit("locality")
    private AccessionCollection collection;

    @OneToMany(mappedBy = "passport", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Stock> stocks = new ArrayList<Stock>();

    @OneToMany(mappedBy = "passport", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Fetch(FetchMode.JOIN)
    private Set<Allele> alleles = new HashSet<Allele>();

    @ManyToOne()
    @JoinColumn(name = "cdv_source_id")
    private Source source;

    @ManyToOne()
    @JoinColumn(name = "div_sampstat_id")
    private Sampstat sampstat;

    private String accename;
    @Column(name = "source")
    private String sourceText;
    private String accenumb;
    private String comments;

    public Passport() {

    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    public AccessionCollection getCollection() {
        return collection;
    }

    public void setCollection(AccessionCollection collection) {
        this.collection = collection;
    }

    public String getAccename() {
        return accename;
    }

    public void setAccename(String accename) {
        this.accename = accename;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getAccenumb() {
        return accenumb;
    }

    public void setAccenumb(String acceNumb) {
        this.accenumb = acceNumb;
    }

    public Sampstat getSampstat() {
        return sampstat;
    }

    public void setSampstat(Sampstat sampstat) {
        this.sampstat = sampstat;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<Stock> getStocks() {
        return Collections.unmodifiableList(stocks);
    }

    public Set<Allele> getAlleles() {
        return alleles;
    }

    public Set<AlleleAssay> getAlleleAssays() {
        ImmutableSet<AlleleAssay> alleleAssays = null;
        alleleAssays = ImmutableSet.copyOf(Collections2
                .transform(alleles,
                        new Function<Allele, AlleleAssay>() {
                            public AlleleAssay apply(Allele allele) {
                                return allele.getAlleleAssay();
                            }
                        }
                ));
        return alleleAssays;
    }

    @Override
    public XContentBuilder getXContent(XContentBuilder builder) throws IOException {
        if (builder == null)
            builder = XContentFactory.jsonBuilder();
        builder.startObject()
                .field("accename", this.getAccename())
                .field("accenumb", this.getAccenumb())
                .field("div_taxonomy_id", this.getTaxonomy().getId())
                .field("comments", this.getComments());
        if (this.getSampstat() != null) {
            builder.startObject("sampstat")
                    .field("germplasm_type", sampstat.getGermplasmType()).endObject();
        }
        if (this.getAlleleAssays() != null && getAlleleAssays().size() > 0) {
            builder.startArray("allele_assay");
            for (AlleleAssay alleleAssay : getAlleleAssays()) {
                builder.startObject();
                alleleAssay.getXContent(builder);
                builder.endObject();
            }
            builder.endArray();
        }

        if (this.getCollection() != null) {
            builder.startObject("collecting");
            addCollection(builder, this.getCollection());
            builder.endObject();
        }
        return builder;
    }


    private static void addCollection(XContentBuilder builder, AccessionCollection collection) throws IOException {
        builder.field("collector", collection.getCollector())
                .field("collcode", collection.getCollCode())
                        // fix col_date to date
                        //.field("col_date", (String)null)
                .field("collnum", collection.getCollNumb())
                .field("collsrc", collection.getCollSrc());
        if (collection.getLocality() != null) {
            builder.startObject("locality");
            addLocality(builder, collection.getLocality());
            builder.endObject();
        }
    }

    private static void addLocality(XContentBuilder builder, Locality locality) throws IOException {
        builder.field("state_provence", locality.getStateProvince())
                .field("origcty", locality.getOrigcty())
                .field("lo_accession", locality.getLoAccession())
                .field("elevation", locality.getElevation())
                .field("locality_name", locality.getLocalityName())
                .field("country", locality.getCountry())
                .field("city", locality.getCity());
        if (locality.getLatitude() != null && locality.getLongitude() != null) {
            builder.field("location", locality.getLatitude() + "," + locality.getLongitude());
        }
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
        return getTaxonomy().getId().toString();
    }

    @Override
    public String getParentId() {
        return getTaxonomy().getId().toString();
    }
}
