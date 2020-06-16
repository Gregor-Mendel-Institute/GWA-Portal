package com.gmi.nordborglab.browser.client.ui;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 21.10.13
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
public class CircularProgressBar extends Composite {


    interface CircularProgressBarUiBinder extends UiBinder<Widget, CircularProgressBar> {

    }

    public interface MyStyle extends CssResource {

    }

    private static CircularProgressBarUiBinder ourUiBinder = GWT.create(CircularProgressBarUiBinder.class);

    @UiField
    DivElement percentageLb;

    @UiField
    MyStyle style;

    @UiField
    DivElement animate25;
    @UiField
    DivElement animate50;
    @UiField
    DivElement animate75;
    @UiField
    DivElement animate100;
    @UiField
    DivElement spinner25;
    @UiField
    DivElement spinner100;
    @UiField
    DivElement spinner75;
    @UiField
    DivElement spinner50;
    @UiField
    DivElement loaderbg;
    private int progress = 0;
    private boolean hasError = false;

    private Map<Range<Integer>, String> colorRanges;

    public CircularProgressBar() {
        colorRanges = ImmutableMap.<Range<Integer>, String>builder()
                .put(Range.closedOpen(0, 25), "#da4f49")
                .put(Range.closedOpen(25, 50), "yellow")
                .put(Range.closedOpen(50, 75), "#faa732")
                .put(Range.closed(75, 100), "#5bb75b")
                .build();
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    public void setThresholds(Map<Range<Integer>, String> colorRanges) {
        this.colorRanges = colorRanges;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        updateProgress();
    }

    private void updateProgress() {
        String color = getColor();
        spinner25.getStyle().setBorderColor(color);
        spinner50.getStyle().setBorderColor(color);
        spinner75.getStyle().setBorderColor(color);
        spinner100.getStyle().setBorderColor(color);
        percentageLb.getStyle().setColor(color);
        resetTransform(animate25.getStyle());
        resetTransform(animate50.getStyle());
        resetTransform(animate75.getStyle());
        resetTransform(animate100.getStyle());
        double angle = 0;
        if (progress < 25) {
            angle = -90.0 + (progress / 100.0) * 360.0;
            setTransform(animate25.getStyle(), angle);
        } else if (progress >= 25 && progress < 50) {
            angle = -90.0 + ((progress - 25.0) / 100.0) * 360.0;
            setTransform(animate25.getStyle(), 0);
            setTransform(animate50.getStyle(), angle);
        } else if (progress >= 50 && progress < 75) {
            angle = -90.0 + ((progress - 50.0) / 100.0) * 360.0;
            setTransform(animate25.getStyle(), 0);
            setTransform(animate50.getStyle(), 0);
            setTransform(animate75.getStyle(), angle);
        } else if (progress >= 75 && progress <= 100) {
            angle = -90.0 + ((progress - 75.0) / 100.0) * 360.0;
            setTransform(animate25.getStyle(), 0);
            setTransform(animate50.getStyle(), 0);
            setTransform(animate75.getStyle(), 0);
            setTransform(animate100.getStyle(), angle);
        }
        percentageLb.setInnerText(progress + " %");
    }

    private void resetTransform(Style style) {
        style.clearProperty("transform");
        style.clearProperty("webkitTransform");
        style.clearProperty("oTransform");
        style.clearProperty("mozTransform");
    }

    private void setTransform(Style style, double deg) {
        style.setProperty("transform", "rotate(" + deg + "deg)");
        style.setProperty("webkitTransform", "rotate(" + deg + "deg)");
        style.setProperty("oTransform", "rotate(" + deg + "deg)");
        style.setProperty("mozTransform", "rotate(" + deg + "deg)");
    }

    private String getColor() {
        if (hasError)
            return "#da4f49";
        for (Map.Entry<Range<Integer>, String> range : colorRanges.entrySet()) {
            if (range.getKey().encloses(Range.singleton(progress))) {
                return range.getValue();
            }
        }
        return "rgba(0,0,0,0.4)";
    }

    public void setSize(int size) {
        getElement().getStyle().setWidth(size, Style.Unit.PX);
        getElement().getStyle().setHeight(size, Style.Unit.PX);
    }

    public void setCircleSize(int size) {
        spinner25.getStyle().setBorderWidth(size, Style.Unit.PX);
        spinner50.getStyle().setBorderWidth(size, Style.Unit.PX);
        spinner75.getStyle().setBorderWidth(size, Style.Unit.PX);
        spinner100.getStyle().setBorderWidth(size, Style.Unit.PX);
        loaderbg.getStyle().setBorderWidth(size, Style.Unit.PX);
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }


}