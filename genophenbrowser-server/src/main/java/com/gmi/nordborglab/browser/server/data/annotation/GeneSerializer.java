package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.elasticsearch.common.jackson.core.JsonGenerationException;
import org.elasticsearch.common.jackson.core.JsonGenerator;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 6:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneSerializer extends JsonSerializer<Gene> {

    public GeneSerializer() {
    }

    @Override
    public void serialize(Gene value, com.fasterxml.jackson.core.JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeNumber(value.getStart());
        jgen.writeNumber(value.getEnd());
        jgen.writeNumber(value.getStrand());
        jgen.writeString(value.getName());
        jgen.writeObject(value.getFeatures());
        jgen.writeEndArray();
    }
}
