package com.gmi.nordborglab.browser.server.domain.meta;

import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
import com.google.auto.value.AutoValue;

/**
 * Created by uemit.seren on 1/15/16.
 */

@AutoValue
public abstract class Association {

    Association() {
    }

    public abstract SNPInfo getSnpInfo();

    public abstract Double getPValue();

    public abstract boolean isOverFDR();

    public abstract Double getMaf();

    public abstract Integer getMac();

    public static Builder builder() {
        return new AutoValue_Association.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setSnpInfo(SNPInfo info);

        public abstract Builder setPValue(Double pValue);

        public abstract Builder setOverFDR(boolean isOverFDR);

        public abstract Builder setMac(Integer mac);

        public abstract Builder setMaf(Double maf);

        public abstract Association build();
    }
}
