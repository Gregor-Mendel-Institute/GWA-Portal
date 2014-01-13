package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface CdvService {

    public StudyPage findStudiesByPhenotypeId(Long id, int start, int size);


    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public Study findStudy(Long id);

    @PreAuthorize("hasRole('ROLE_USER') and (#study.id == null or hasPermission(#study,'EDIT') or hasPermission(#study,'ADMINISTRATION'))")
    public Study saveStudy(Study study);

    @PostFilter("hasPermission(filterObject,'READ')")
    public List<Study> findStudiesByPassportId(Long passportId);

    public StudyPage findAll(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    public StudyPage findAll(Long phenotypeId, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    @PreAuthorize("hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public List<Trait> findTraitValues(Long studyId);

    public List<AlleleAssay> findAlleleAssaysWithStats(Long phenotypeId, Long statisticTypeId);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','EDIT') or hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','ADMINISTRATION'))")
    public Study createStudyJob(Long studyId);

    @PreAuthorize("hasRole('ROLE_USER') and (#study.id == null or hasPermission(#study,'EDIT') or hasPermission(#study,'ADMINISTRATION'))")
    public void delete(Study study);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','EDIT') or hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','ADMINISTRATION'))")
    Study deleteStudyJob(Long studyId);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','EDIT') or hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','ADMINISTRATION'))")
    Study rerunAnalysis(Long studyId);
}
