package com.gmi.nordborglab.browser.server.data.annotation.nclist;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCListItemSerializer extends JsonSerializer<NCListItem> {

    public NCListItemSerializer() {
    }


    @Override
    public void serialize(NCListItem value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeNumber(value.getStart());
        jgen.writeNumber(value.getEnd());
        jgen.writeNumber(value.getStrand());
        jgen.writeNumber(value.getName());
        jgen.writeObject(value.getGeneFeatures());
        jgen.writeEndArray();
    }
}
