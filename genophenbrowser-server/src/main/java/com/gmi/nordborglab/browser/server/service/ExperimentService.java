package com.gmi.nordborglab.browser.server.service;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;

import java.util.List;

public interface ExperimentService {

	ExperimentPage findByAcl(int page, int size);

    List<Experiment> findAllByAcl(Integer permission);
	
	@PreAuthorize("hasRole('ROLE_USER') and (#experiment.id == null or hasPermission(#experiment,'EDIT') or hasPermission(#experiment,'ADMINISTRATION'))")
	Experiment save(Experiment experiment);
	
    @PreAuthorize("hasPermission(#id,'com.gmi.nordborglab.browser.server.domain.observation.Experiment','READ')")
	Experiment findExperiment(Long id);
	
	
}
