package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.pages.PublicationPage;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;

import java.util.List;
import java.util.Set;

public interface ExperimentService {


    ExperimentPage findByAclAndFilter(ConstEnums.TABLE_FILTER filter, String searchString, int page, int size);

    List<Experiment> findAllByAcl(Integer permission);

    @PreAuthorize("hasRole('ROLE_USER') and (#experiment.id == null or hasPermission(#experiment,'EDIT') or hasPermission(#experiment,'ADMINISTRATION'))")
    Experiment save(Experiment experiment);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#experiment,'EDIT') or hasPermission(#experiment,'ADMINISTRATION'))")
    void delete(Experiment experiment);

    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.observation.Experiment','READ')")
    Experiment findExperiment(Long id);


    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.observation.Experiment','EDIT')")
    Experiment addPublication(Long id, Publication publication);

    PublicationPage getPublications(int start, int size);

    Publication findOnePublication(Long id);

    @PostFilter("hasPermission(filterObject,'READ')")
    Set<Experiment> findExperimentsByPublication(Long id);
}
