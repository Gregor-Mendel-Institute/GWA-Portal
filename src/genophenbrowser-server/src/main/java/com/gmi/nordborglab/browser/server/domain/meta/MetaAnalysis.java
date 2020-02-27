package com.gmi.nordborglab.browser.server.domain.meta;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

/**
 * Created by uemit.seren on 1/15/16.
 */

@AutoValue
public abstract class MetaAnalysis {

    MetaAnalysis() {
    }

    public abstract String getAnalysis();

    public abstract Long getAnalysisId();

    public abstract String getPhenotype();

    public abstract Long getPhenotypeId();

    public abstract String getStudy();

    public abstract Long getStudyId();

    public abstract String getMethod();

    public abstract String getGenotype();

    @Nullable
    public abstract Long getTotalAssocCount();

    public abstract ImmutableList<Association> getAssociations();

    public static Builder builder() {
        return new AutoValue_MetaAnalysis.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setTotalAssocCount(Long assocCount);

        public abstract Builder setAnalysis(String analysis);

        public abstract Builder setAnalysisId(Long analysisId);

        public abstract Builder setPhenotype(String phenotype);

        public abstract Builder setPhenotypeId(Long phenotypeId);

        public abstract Builder setStudy(String study);

        public abstract Builder setStudyId(Long studyId);

        public abstract Builder setMethod(String method);

        public abstract Builder setGenotype(String String);

        public abstract Builder setAssociations(ImmutableList<Association> associations);

        public abstract MetaAnalysis build();
    }

}
