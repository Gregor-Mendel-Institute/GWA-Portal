package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/5/13
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeCardRenderer {

    public interface MyStyle extends CssResource {

    }

    public interface Renderer extends UiRenderer {
        void render(SafeHtmlBuilder sb, SafeHtml name,String traitOntology,String unitType,String numberOfStudies,String numberOfObsUnits);
        MyStyle getStyle();
    }

    private static final String replaceString = "<span style='color:red;font-weight:bold;'>$1</span>";
    private final Renderer uiRenderer;


    @Inject
    public PhenotypeCardRenderer(final Renderer uiRenderer) {
        super();
        this.uiRenderer = uiRenderer;
    }


    public void render(SearchTerm searchTerm,PhenotypeProxy phenotype,SafeHtmlBuilder sb) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        if (searchTerm != null) {
            RegExp searchRegExp = searchTerm.getSearchRegExp();
            if (searchRegExp != null) {
                String value = searchRegExp.replace(SafeHtmlUtils.htmlEscape(phenotype.getLocalTraitName()),replaceString);
                builder.append(SafeHtmlUtils.fromTrustedString(value));
            }
            else {
                builder.appendEscaped(phenotype.getLocalTraitName());
            }
        } else {
            builder.appendEscaped(phenotype.getLocalTraitName());
        }
        SafeHtml name = builder.toSafeHtml();
        String traitOntology = phenotype.getToAccession();
        String unitType = "N/A";
        String numberOfStudies = "0";
        String numberOfObsUnits = "0";
        if (phenotype.getNumberOfStudies() != null)
            numberOfStudies = phenotype.getNumberOfStudies().toString();
        if (phenotype.getNumberOfObsUnits() != null)
            numberOfObsUnits = phenotype.getNumberOfObsUnits().toString();
        if (phenotype.getTraitOntologyTerm() != null)
            traitOntology = phenotype.getTraitOntologyTerm().getName() + " (" + traitOntology + ")";
        uiRenderer.render(sb,name,traitOntology,unitType,numberOfStudies,numberOfObsUnits);
    }


}