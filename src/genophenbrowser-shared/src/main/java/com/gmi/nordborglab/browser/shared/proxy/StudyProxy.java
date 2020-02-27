package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.cdv.Study", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface StudyProxy extends SecureEntityProxy {

    Long getId();

    @NotNull
    StudyProtocolProxy getProtocol();

    void setProtocol(StudyProtocolProxy protocol);

    @NotNull
    AlleleAssayProxy getAlleleAssay();

    void setAlleleAssay(AlleleAssayProxy alleleAssay);

    @NotNull
    String getName();

    void setName(String name);

    String getProducer();

    void setProducer(String producer);

    Date getStudyDate();

    void setStudyDate(Date date);

    AccessControlEntryProxy getUserPermission();

    boolean isOwner();

    void setTraits(Set<TraitProxy> traits);

    Set<TraitProxy> getTraits();

    PhenotypeProxy getPhenotype();

    void setTransformation(TransformationProxy transformation);

    TransformationProxy getTransformation();

    StudyJobProxy getJob();

    void setJob(StudyJobProxy studyJob);

    Date getCreated();

    Date getPublished();

    Date getModified();

    void setCreateEnrichments(boolean isCreateEnrichments);

    boolean isCreateEnrichments();

    Double getPseudoHeritability();

    Double getShapiroWilkPvalue();

    void setShapiroWilkPvalue(Double shapiroWilkPvalue);
}
