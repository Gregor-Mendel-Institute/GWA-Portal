package com.gmi.nordborglab.browser.server.domain.meta;

import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class MetaSNPAnalysis {

    protected SNPAnnot snpAnnotation;
    protected Double pValue;
    protected String analysis;
    protected Long analysisId;
    protected String phenotype;
    protected Long phenotypeId;
    protected String study;
    protected Long studyId;
    protected String method;
    protected String genotype;
    protected Boolean isOverFDR;
    protected Double maf;
    protected Integer mac;

    public static class Builder {

        private SNPAnnot snpAnnotation;
        private Double pValue;
        private String analysis;
        private String phenotype;
        private String study;
        private String method;
        private String genotype;
        private Boolean overFDR;
        private Long studyId;
        private Long phenotypeId;
        private Long analysisId;
        private Integer mac;
        private Double maf;

        public Builder setSnpAnnotation(SNPAnnot snpAnnotation) {
            this.snpAnnotation = snpAnnotation;
            return this;
        }

        public Builder setpValue(Double pValue) {
            this.pValue = pValue;
            return this;
        }

        public Builder setAnalysis(String analysis) {
            this.analysis = analysis;
            return this;
        }

        public Builder setPhenotype(String phenotype) {
            this.phenotype = phenotype;
            return this;
        }

        public Builder setStudy(String study) {
            this.study = study;
            return this;
        }

        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder setGenotype(String genotype) {
            this.genotype = genotype;
            return this;
        }

        public Builder setOverFDR(Boolean overFDR) {
            this.overFDR = overFDR;
            return this;
        }

        public Builder setPhenotypeId(Long phenotypeId) {
            this.phenotypeId = phenotypeId;
            return this;
        }

        public Builder setStudyId(Long studyId) {
            this.studyId = studyId;
            return this;
        }

        public Builder setAnalysisId(Long analysisId) {
            this.analysisId = analysisId;
            return this;
        }

        public Builder setMac(Integer mac) {
            this.mac = mac;
            return this;
        }

        public Builder setMaf(double maf) {
            this.maf = maf;
            return this;
        }

        public MetaSNPAnalysis build() {
            return new MetaSNPAnalysis(this);
        }


    }

    public MetaSNPAnalysis() {

    }

    private MetaSNPAnalysis(Builder builder) {
        this.snpAnnotation = builder.snpAnnotation;
        this.pValue = builder.pValue;
        this.analysis = builder.analysis;
        this.phenotype = builder.phenotype;
        this.study = builder.study;
        this.method = builder.method;
        this.genotype = builder.genotype;
        this.isOverFDR = builder.overFDR;
        this.analysisId = builder.analysisId;
        this.phenotypeId = builder.phenotypeId;
        this.studyId = builder.studyId;
        this.mac = builder.mac;
        this.maf = builder.maf;
    }

    public SNPAnnot getSnpAnnotation() {
        return snpAnnotation;
    }


    public Double getPValue() {
        return pValue;
    }

    public String getAnalysis() {
        return analysis;
    }

    public String getPhenotype() {
        return phenotype;
    }

    public String getStudy() {
        return study;
    }

    public String getMethod() {
        return method;
    }

    public String getGenotype() {
        return genotype;
    }

    public Boolean isOverFDR() {
        return isOverFDR;
    }

    public Long getAnalysisId() {
        return analysisId;
    }

    public Long getPhenotypeId() {
        return phenotypeId;
    }

    public Long getStudyId() {
        return studyId;
    }

    public Double getMaf() {
        return maf;
    }

    public Integer getMac() {
        return mac;
    }
}
