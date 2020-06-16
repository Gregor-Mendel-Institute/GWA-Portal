package com.gmi.nordborglab.browser.client.ui.favicon;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 18.10.13
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class FavicoOptions extends JavaScriptObject {

    protected FavicoOptions() {

    }

    public static enum TYPE {CIRCLE, RECTANGLE}

    public static enum POSITION {UP, DOWN}

    public static enum ANIMATION {SLIDE, FADE, POP, POPFADE, NONE}


    public static FavicoOptions create() {
        return JavaScriptObject.createObject().cast();
    }

    public final native FavicoOptions set(String key, String value) /*-{
        this[key] = value;
        return this;
    }-*/;

    public final FavicoOptions setBgColor(String bgColor) {
        return set("bgColor", bgColor);
    }

    public final FavicoOptions setTextColor(String textColor) {
        return set("textColor", textColor);
    }

    public final FavicoOptions setFontFamily(String fontFamily) {
        return set("fontFamily", fontFamily);
    }

    public final FavicoOptions setFontStyle(String fontStyle) {
        return set("fontStyle", fontStyle);
    }

    public final FavicoOptions setType(TYPE type) {
        return set("type", type.name().toLowerCase());
    }

    public final FavicoOptions setPosition(POSITION position) {
        return set("position", position.name().toLowerCase());
    }

    public final FavicoOptions setAnimation(ANIMATION animation) {
        return set("animation", animation.name().toLowerCase());
    }

    public final FavicoOptions setElementId(String elementId) {
        return set("elementId", elementId);
    }


}
