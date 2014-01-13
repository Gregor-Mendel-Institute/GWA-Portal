package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneFeatureSerializer extends JsonSerializer<GeneFeature> {

    public GeneFeatureSerializer() {

    }

    @Override
    public void serialize(GeneFeature value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeStartArray();
        jgen.writeNumber(value.getStart());
        jgen.writeNumber(value.getEnd());
        jgen.writeNumber(value.getStrand());
        jgen.writeString(value.getName());
        jgen.writeEndArray();
    }
}
