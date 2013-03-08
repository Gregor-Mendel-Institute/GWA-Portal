package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.gmi.nordborglab.browser.server.data.annotation.GeneFeature;


import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneFeatureDeserializer extends JsonDeserializer<GeneFeature> {


    @Override
    public GeneFeature deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        Long start = null;
        Long end = null;
        int strand =0;
        String type = null;
        int i = 0;
        while (jp.nextToken() != JsonToken.END_ARRAY) {
            if (i == 0) {
                start = jp.getValueAsLong();
            } else if (i==1) {
                end = jp.getValueAsLong();
            }
            else if (i==2) {
                strand = jp.getValueAsInt();
            }
            else if (i==3) {
                type = jp.getText();
            }
            i= i+1;
        }
        GeneFeature item = new GeneFeature(start,end,strand,type);
        return item;
    }
}
