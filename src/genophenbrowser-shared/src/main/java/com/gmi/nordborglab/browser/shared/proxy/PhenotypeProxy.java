package com.gmi.nordborglab.browser.shared.proxy;

import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Set;


@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface PhenotypeProxy extends SecureEntityProxy {

    Long getId();

    UnitOfMeasureProxy getUnitOfMeasure();

    void setUnitOfMeasure(UnitOfMeasureProxy unitOfMeasure);

    Set<TraitProxy> getTraits();

    List<StatisticTypeProxy> getStatisticTypes();

    List<Long> getStatisticTypeTraitCounts();

    @NotNull
    String getLocalTraitName();

    void setLocalTraitName(String localTraitName);

    String getTraitProtocol();

    void setTraitProtocol(String traitProtocol);

    String getToAccession();

    void setToAccession(String toAccession);

    TermProxy getTraitOntologyTerm();

    void setTraitOntologyTerm(TermProxy term);

    TermProxy getEnvironOntologyTerm();

    void setEnvironOntologyTerm(TermProxy term);

    String getEoAccession();

    void setEoAccession(String eoAccession);

    boolean isOwner();

    AccessControlEntryProxy getUserPermission();

    Long getNumberOfObsUnits();

    Long getNumberOfStudies();

    ExperimentProxy getExperiment();

    Date getCreated();

    Date getPublished();

    Date getModified();
}
