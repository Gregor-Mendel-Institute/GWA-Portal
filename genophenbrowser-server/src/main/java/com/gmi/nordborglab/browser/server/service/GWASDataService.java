package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;
import java.util.List;

public interface GWASDataService {


    @PreAuthorize("hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public GWASData getGWASDataByStudyId(Long studyId);

    @PreAuthorize("hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public GWASData getGWASDataByStudyId(Long studyId, Double limit);

    @PreAuthorize("hasPermission(#gwasResultId,'com.gmi.nordborglab.browser.server.domain.util.GWASResult','READ')")
    public GWASData getGWASDataByViewerId(Long gwasResultId);

    @PreAuthorize("hasRole('ROLE_USER')")
    public GWASResult uploadGWASResult(CommonsMultipartFile file) throws IOException;

    @PreAuthorize("hasRole('ROLE_USER')")
    public List<GWASResult> findAllGWASResults();

    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.util.GWASResult','READ')")
    public GWASResult findOneGWASResult(Long id);

    @PreAuthorize("hasRole('ROLE_USER') AND hasPermission(#gwasResult,'EDIT')")
    public List<GWASResult> delete(GWASResult gwasResult);

    @PreAuthorize("hasRole('ROLE_USER')  AND hasPermission(#gwasResult,'EDIT')")
    public GWASResult save(GWASResult gwasResult);

    @PreAuthorize("hasRole('ROLE_USER') AND hasPermission(#studyId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ') ")
    Study uploadStudyGWASResult(Long studyId, CommonsMultipartFile file) throws IOException;

    public void deleteStudyFile(Long id);

    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    String getHDF5StudyFile(Long id);
}
