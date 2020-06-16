package com.gmi.nordborglab.browser.server.data.annotation.nclist;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Created by uemit.seren on 4/22/16.
 */
@AutoValue
public abstract class FeatureClasses {

    public FeatureClasses() {

    }


    public abstract Integer getStartIdx();

    public abstract Integer getEndIdx();

    @Nullable
    public abstract Integer getStrandIdx();

    @Nullable
    public abstract Integer getNameIdx();


    @Nullable
    public abstract Integer getTypeIdx();


    @Nullable
    public abstract Integer getSubfeatureIdx();


    public static Builder builder() {
        return new AutoValue_FeatureClasses.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setStartIdx(Integer startIdx);

        public abstract Builder setEndIdx(Integer endIdx);


        public abstract Builder setStrandIdx(Integer strandIdx);

        public abstract Builder setNameIdx(Integer nameIdx);

        public abstract Builder setSubfeatureIdx(Integer subfeatureIdx);

        public abstract Builder setTypeIdx(Integer typeIdx);


        public abstract FeatureClasses build();
    }

}
