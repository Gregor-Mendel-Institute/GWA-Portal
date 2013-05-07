package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;

public interface TraitUomService {
	TraitUomPage findPhenotypesByExperiment(Long id,int page, int size);
	int countPhenotypeByExperimentCount(Long id);
	
	@PostAuthorize("hasPermission(returnObject,'READ')")
	TraitUom findPhenotype(Long id);
	
	@PreAuthorize("hasRole('ROLE_USER') and (#trait.id != null and (hasPermission(#trait,'EDIT') or hasPermission(#trait,'ADMINISTRATION')))")
	TraitUom save(TraitUom trait);
	
	List<TraitUom> findPhenotypesByPassportId(Long passportId);
	
	TraitUomPage findAll(String name,String experiment,String ontology,String protocol,int start,int size);

    List<TraitUom> findPhenotypesByExperimentAndAcl(Long id,int permission);

    @PreAuthorize("hasRole('ROLE_USER')")
    Long savePhenotypeUploadData(Long experimentId,PhenotypeUploadData data);

    @PostFilter("hasPermission(filterObject,'READ')")
    List<TraitUom> findAllByOntology(String type,String acc,boolean checkChilds);
}
