package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;

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

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#experimentId,'com.gmi.nordborglab.browser.server.domain.observation.Experiment','EDIT') or hasPermission(#experimentId,'com.gmi.nordborglab.browser.server.domain.observation.Experiment','ADMINISTRATION'))")
    Long savePhenotypeUploadData(Long experimentId, PhenotypeUploadData data);

    @PostFilter("hasPermission(filterObject,'READ')")
    List<TraitUom> findAllByOntology(String type, String acc, boolean checkChilds);

}
