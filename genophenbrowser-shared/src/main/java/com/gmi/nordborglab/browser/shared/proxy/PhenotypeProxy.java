package com.gmi.nordborglab.browser.shared.proxy;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import javax.validation.constraints.NotNull;


@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface PhenotypeProxy extends SecureEntityProxy {

    Long getId();

    @NotNull
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
