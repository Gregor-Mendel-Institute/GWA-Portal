package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.google.gwt.cell.client.AbstractCell;
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
public class GenotypeCard extends AbstractCell<AlleleAssayProxy> {

    public interface MyStyle extends CssResource {

    }

    public interface Renderer extends UiRenderer {
        void render(SafeHtmlBuilder sb, String name, String producer, String scoringTechType, String polyType, String overlap, String overlapLabelStyle);

        MyStyle getStyle();
    }

    private static final String replaceString = "<span style='color:red;font-weight:bold;'>$1</span>";
    private final Renderer uiRenderer;

    @Inject
    public GenotypeCard(final Renderer uiRenderer) {
        super();
        this.uiRenderer = uiRenderer;
    }

    @Override
    public void render(Context context, AlleleAssayProxy alleleAssay, SafeHtmlBuilder sb) {
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
        String overlap = "100 % (200/200)";
        String overlapLabelStyle = "success";
        uiRenderer.render(sb, name, producer, scoringTechType, polyType, overlap,overlapLabelStyle);
    }
}
