package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.dom.client.Style;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
        void render(SafeHtmlBuilder sb, SafeHtml name, String traitOntology, String unitType, String numberOfStudies, String numberOfObsUnits,
                    String permission, String permissionStyle, String cardContentStyle, String newCardContentStyle);

        MyStyle getStyle();
    }

    private static final String replaceString = "<span style='color:red;font-weight:bold;'>$1</span>";
    private final Renderer uiRenderer;


    @Inject
    public PhenotypeCardRenderer(final Renderer uiRenderer) {
        super();
        this.uiRenderer = uiRenderer;
    }


    public void render(SearchTerm searchTerm, PhenotypeProxy phenotype, SafeHtmlBuilder sb) {
        SafeStyles cardContentStyle = (phenotype.getId() != null ? SafeStylesUtils.forDisplay(Style.Display.BLOCK) : SafeStylesUtils.forDisplay(Style.Display.NONE));
        SafeStyles newCardContentStyle = (phenotype.getId() != null ? SafeStylesUtils.forDisplay(Style.Display.NONE) : SafeStylesUtils.forDisplay(Style.Display.BLOCK));
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        SafeHtml name = null;
        String traitOntology = "N/A";
        String unitType = "N/A";
        String numberOfStudies = "0";
        String numberOfObsUnits = "0";
        String permission = "PUBLIC";
        String permissionStyle = "label-success";
        if (phenotype.getId() != null) {
            if (searchTerm != null) {
                RegExp searchRegExp = searchTerm.getSearchRegExp();
                if (searchRegExp != null) {
                    String value = searchRegExp.replace(SafeHtmlUtils.htmlEscape(phenotype.getLocalTraitName()), replaceString);
                    builder.append(SafeHtmlUtils.fromTrustedString(value));
                } else {
                    builder.appendEscaped(phenotype.getLocalTraitName());
                }
            } else {
                builder.appendEscaped(phenotype.getLocalTraitName());
            }
            name = builder.toSafeHtml();
            if (phenotype.getToAccession() != null)
                traitOntology = phenotype.getToAccession();

            if (phenotype.getNumberOfStudies() != null)
                numberOfStudies = phenotype.getNumberOfStudies().toString();
            if (phenotype.getNumberOfObsUnits() != null)
                numberOfObsUnits = phenotype.getNumberOfObsUnits().toString();
            if (phenotype.getTraitOntologyTerm() != null)
                traitOntology = phenotype.getTraitOntologyTerm().getName() + " (" + traitOntology + ")";
            if (!phenotype.isPublic()) {
                permission = "RESTRICTED";
                permissionStyle = "label-warning";
            }
        } else {
            name = SafeHtmlUtils.fromSafeConstant("");
        }
        // asString() required because of https://groups.google.com/forum/#!topic/google-web-toolkit/snEC1ZiQJT8
        uiRenderer.render(sb, name, traitOntology, unitType, numberOfStudies, numberOfObsUnits, permission, permissionStyle, cardContentStyle.asString(), newCardContentStyle.asString());
    }


}