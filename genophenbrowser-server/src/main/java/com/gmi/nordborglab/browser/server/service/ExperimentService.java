package com.gmi.nordborglab.browser.server.service;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;

public interface ExperimentService {

	ExperimentPage findByAcl(int page, int size);
	
	@PreAuthorize("hasRole('ROLE_USER') and (#experiment.id == null or hasPermission(#experiment,'WRITE') or hasPermission(#experiment,'ADMINISTRATION'))")
	Experiment save(Experiment experiment);
	
	@PostAuthorize("hasPermission(returnObject,'READ')")
	Experiment findExperiment(Long id);
	
	
}
