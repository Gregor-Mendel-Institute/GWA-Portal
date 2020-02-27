package com.gmi.nordborglab.browser.server.es;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Created by uemit.seren on 1/28/15.
 */
public interface ESDocument {

    XContentBuilder getXContent(XContentBuilder builder) throws IOException;

    String getEsType();

    String getEsId();

    String getRouting();

    String getParentId();

}
