package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Created by uemit.seren on 5/12/16.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public interface LDGlobal {
    @JsProperty
    float[] getR2();

    @JsProperty
    int[] getSnps();
}
