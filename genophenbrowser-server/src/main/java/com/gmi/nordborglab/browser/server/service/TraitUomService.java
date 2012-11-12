package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;

public interface TraitUomService {
	TraitUomPage findPhenotypesByExperiment(Long id,int page, int size);
	int countPhenotypeByExperimentCount(Long id);
	
	@PostAuthorize("hasPermission(returnObject,'READ')")
	TraitUom findPhenotype(Long id);
	
	@PreAuthorize("hasRole('ROLE_USER') and (#trait.id != null and (hasPermission(#trait,'WRITE') or hasPermission(#trait,'ADMINISTRATION')))")
	TraitUom save(TraitUom trait);
	
	List<TraitUom> findPhenotypesByPassportId(Long passportId);
}
