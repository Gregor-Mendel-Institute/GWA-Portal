package com.gmi.nordborglab.browser.server.domain;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.google.common.base.Function;

import java.util.Set;

/**
 * Created by uemit.seren on 2/2/15.
 */
public final class DomainFunctions {


    private static Function<CandidateGeneListEnrichment, ? extends Set<Long>> candidateGeneListEnrichmentId;

    private DomainFunctions() { /* prevents instantiation */ }

    /**
     * @return a {@link Function} that returns an {@link BaseEntity}'s id.
     */
    public static Function<Experiment, Long> getExperimentId() {
        return GetExperimentIdFunction.INSTANCE;
    }

    public static Function<TraitUom, Long> getTraitUomId() {
        return GetTraitUomIdFunction.INSTANCE;
    }

    public static Function<Passport, Long> getPassportId() {
        return GetPassportIdFunction.INSTANCE;
    }

    public static Function<Study, Long> getStudyId() {
        return GetStudyIdFunction.INSTANCE;
    }

    public static Function<AppUser, Long> getUserId() {
        return GetUserIdFunction.INSTANCE;
    }

    public static Function<CandidateGeneList, Long> getCandidateGeneListId() {
        return GetCandidateGeneListIdFunction.INSTANCE;
    }

    public static Function<CandidateGeneListEnrichment, Long> getCandidateGeneListEnrichmentId() {
        return GetCandidateGeneListEnrichmentIdFunction.INSTANCE;
    }

    // enum singleton pattern
    private enum GetPassportIdFunction implements Function<Passport, Long> {
        INSTANCE;

        public Long apply(Passport item) {
            return item.getId();
        }
    }


    // enum singleton pattern
    private enum GetExperimentIdFunction implements Function<Experiment, Long> {
        INSTANCE;

        public Long apply(Experiment item) {
            return item.getId();
        }
    }

    private enum GetTraitUomIdFunction implements Function<TraitUom, Long> {
        INSTANCE;

        public Long apply(TraitUom item) {
            return item.getId();
        }
    }

    private enum GetStudyIdFunction implements Function<Study, Long> {
        INSTANCE;

        public Long apply(Study item) {
            return item.getId();
        }
    }

    private enum GetUserIdFunction implements Function<AppUser, Long> {
        INSTANCE;

        public Long apply(AppUser item) {
            return item.getId();
        }
    }

    private enum GetCandidateGeneListIdFunction implements Function<CandidateGeneList, Long> {
        INSTANCE;

        public Long apply(CandidateGeneList item) {
            return item.getId();
        }
    }

    private enum GetCandidateGeneListEnrichmentIdFunction implements Function<CandidateGeneListEnrichment, Long> {
        INSTANCE;

        public Long apply(CandidateGeneListEnrichment item) {
            return item.getId();
        }
    }
}
