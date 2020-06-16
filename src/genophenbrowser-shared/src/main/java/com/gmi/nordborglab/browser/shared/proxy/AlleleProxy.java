package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/24/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.genotype.Allele", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface AlleleProxy extends EntityProxy{

    public Long getId();

    public AlleleAssayProxy getAlleleAssay();

    public void setAlleleAssay(AlleleAssayProxy alleleAssay);

    public Integer getAlleleNum();

    public void setAlleleNum(Integer alleleNum);

    public String getAccession();

    public void setAccession(String accession);

    public String getReferencedb();

    public void setReferencedb(String referencedb);

    public void setPassport(PassportProxy passport);

    public PassportProxy getPassport();
}
