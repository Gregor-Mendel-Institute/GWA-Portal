package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.google.common.collect.ImmutableMap;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;
import java.util.List;

public interface GWASDataService {

	public ImmutableMap<String,GWASData> getGWASDataByStudyId(Long studyId);

    public ImmutableMap<String,GWASData> getGWASDataByViewerId(Long gwasResultId);

    @PreAuthorize("hasRole('ROLE_USER')")
    public GWASResult uploadGWASResult(CommonsMultipartFile file) throws IOException;

    @PreAuthorize("hasRole('ROLE_USER')")
    public List<GWASResult> findAllGWASResults();


    @PreAuthorize("hasRole('ROLE_USER') AND hasPermission(#gwasResult,'DELETE')")
    public List<GWASResult> delete(GWASResult gwasResult);
}
