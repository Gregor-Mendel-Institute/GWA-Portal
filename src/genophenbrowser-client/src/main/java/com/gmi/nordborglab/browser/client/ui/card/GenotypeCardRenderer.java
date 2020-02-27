package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.inject.Inject;


/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/6/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenotypeCardRenderer {

    public interface MyStyle extends CssResource {

    }

    public interface Renderer extends UiRenderer {
        void render(SafeHtmlBuilder sb, String name, String producer, String scoringTechType, String polyType, String overlap, String overlapLabelStyle);
        MyStyle getStyle();
    }
    private final Renderer uiRenderer;

    @Inject
    public GenotypeCardRenderer(final Renderer uiRenderer) {
        super();
        this.uiRenderer = uiRenderer;
    }


    public void render(AlleleAssayProxy alleleAssay,SafeHtmlBuilder sb) {
        if (alleleAssay == null)
            return;
        String name = alleleAssay.getName();
        String producer = alleleAssay.getProducer();
        String polyType= "N/A";
        String scoringTechType = "N/A";
        if (alleleAssay.getPolyType() != null)
            polyType = alleleAssay.getPolyType().getPolyType();
        if (alleleAssay.getScoringTechType() != null)
            scoringTechType = alleleAssay.getScoringTechType().getScoringTechGroup();

        long availableAllelesCount = alleleAssay.getAvailableAllelesCount();
        long traitValuesCount = alleleAssay.getTraitValuesCount();
        long percentage = 0;
        String overlapLabelStyle = "success";

        if (traitValuesCount > 0) {
            double fraction = ((double)availableAllelesCount/(double)traitValuesCount)*100;
            percentage = Math.round(fraction);
        }
        if (percentage < 20) {
            overlapLabelStyle = "danger";
        }
        else if (percentage < 100) {
            overlapLabelStyle = "warning";
        }
        String overlap = percentage+" % ("+availableAllelesCount+"/"+traitValuesCount+")";
        uiRenderer.render(sb, name, producer, scoringTechType, polyType, overlap,overlapLabelStyle);
    }
}
