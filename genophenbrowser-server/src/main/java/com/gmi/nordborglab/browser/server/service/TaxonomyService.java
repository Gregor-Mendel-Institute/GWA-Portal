package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.domain.stats.TaxonomyStats;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TaxonomyService {

    public List<Taxonomy> findAll();

    public Taxonomy findOne(Long id);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Taxonomy save(Taxonomy taxonomy);

    public TaxonomyStats findStats(Long id);

}
