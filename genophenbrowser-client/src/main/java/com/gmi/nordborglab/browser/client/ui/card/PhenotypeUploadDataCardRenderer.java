package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.inject.Inject;

/**
 * Created by uemit.seren on 6/30/14.
 */
public class PhenotypeUploadDataCardRenderer {

    public interface MyStyle extends CssResource {

    }

    public interface Renderer extends UiRenderer {

        void render(SafeHtmlBuilder sb, SafeHtml name, String traitOntology, String unitType, String numberOfObsUnits, String status, String statusStyle, String statusIconStyle);

        MyStyle getStyle();

    }

    private final Renderer uiRenderer;
    private static final String replaceString = "<span style='color:red;font-weight:bold;'>$1</span>";


    @Inject
    public PhenotypeUploadDataCardRenderer(final Renderer uiRenderer) {
        super();
        this.uiRenderer = uiRenderer;
    }

    public void render(SearchTerm searchTerm, PhenotypeUploadDataProxy phenotype, SafeHtmlBuilder sb) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        SafeHtml name = null;
        String traitOntology = "N/A";
        String unitType = "N/A";
        String numberOfObsUnits = "0";
        String status = "All values valid";
        String statusStyle = "alert-success";
        String statusIconStyle = "icon-ok-circle";
        String nameTxt = phenotype.getTraitUom().getLocalTraitName();
        // required otherwise appendEscaped throws error
        if (nameTxt == null)
            nameTxt = "";
        if (searchTerm != null) {
            RegExp searchRegExp = searchTerm.getSearchRegExp();
            if (searchRegExp != null) {
                String value = searchRegExp.replace(SafeHtmlUtils.htmlEscape(nameTxt), replaceString);
                builder.append(SafeHtmlUtils.fromTrustedString(value));
            } else {
                builder.appendEscaped(nameTxt);
            }
        } else {
            builder.appendEscaped(nameTxt);
        }
        name = builder.toSafeHtml();
        if (phenotype.getTraitUom().getUnitOfMeasure() != null) {
            unitType = phenotype.getTraitUom().getUnitOfMeasure().getUnitType();
        }
        if (phenotype.getTraitUom().getTraitOntologyTerm() != null)
            traitOntology = phenotype.getTraitUom().getTraitOntologyTerm().getName() + " (" + phenotype.getTraitUom().getTraitOntologyTerm().getAcc() + ")";
        numberOfObsUnits = String.valueOf(phenotype.getValueCount());
        if (phenotype.getErrorCount() > 0) {
            status = phenotype.getErrorCount() + " values have errors";
            statusIconStyle = "icon-warning-sign";
            statusStyle = "alert-warning";
        } else if (phenotype.getConstraintViolation()) {
            status = "Some information are wrong or missing";
            statusIconStyle = "icon-error-sign";
            statusStyle = "alert-error";
        }
        // asString() required because of https://groups.google.com/forum/#!topic/google-web-toolkit/snEC1ZiQJT8
        uiRenderer.render(sb, name, traitOntology, unitType, numberOfObsUnits, status, statusStyle, statusIconStyle);
    }
}