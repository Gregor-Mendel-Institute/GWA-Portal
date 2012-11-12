package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.TaxonomyStats;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;

public interface TaxonomyService {
	
	public List<Taxonomy> findAll();
	
	public Taxonomy findOne(Long id);
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Taxonomy save(Taxonomy taxonomy);
	
	public TaxonomyStats findStats(Long id);

}
