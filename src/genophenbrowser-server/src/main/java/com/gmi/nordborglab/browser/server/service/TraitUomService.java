package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TraitUomService {

    TraitUomPage findPhenotypesByExperiment(Long id, int page, int size);

    int countPhenotypeByExperimentCount(Long id);

    @PostAuthorize("hasPermission(returnObject,'READ')")
    TraitUom findPhenotype(Long id);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#traitUom,'EDIT') or hasPermission(#traitUom,'ADMINISTRATION'))")
    TraitUom save(TraitUom traitUom);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#traitUom,'EDIT') or hasPermission(#traitUom,'ADMINISTRATION'))")
    void delete(TraitUom traitUom);


    List<TraitUom> findPhenotypesByPassportId(Long passportId);

    TraitUomPage findAll(Long experimentId, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    TraitUomPage findAll(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    List<TraitUom> findPhenotypesByExperimentAndAcl(Long id, int permission);

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#experiment,'EDIT') or hasPermission(#experiment,'ADMINISTRATION'))")
    List<TraitUom> savePhenotypeUploadData(Experiment experiment, List<PhenotypeUploadData> data, List<SampleData> samples);

    @PostFilter("hasPermission(filterObject,'READ')")
    List<TraitUom> findAllByOntology(String type, String acc, boolean checkChilds);

}
