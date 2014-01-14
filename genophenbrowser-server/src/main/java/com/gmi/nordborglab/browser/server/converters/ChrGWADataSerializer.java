package com.gmi.nordborglab.browser.server.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.gmi.nordborglab.browser.server.data.ChrGWAData;

import java.io.IOException;

/**
 * Created by uemit.seren on 14.01.14.
 */
public class ChrGWADataSerializer extends JsonSerializer<ChrGWAData> {

    @Override
    public void serialize(ChrGWAData value, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        char chr = value.getChr().charAt(3);
        boolean hasGVE = value.getGVEs() != null;
        for (int i =0;i<value.getPositions().length;i++) {
            jgen.writeStartArray();
                jgen.writeRaw(chr);
                jgen.writeNumber(value.getPositions()[i]);
                jgen.writeNumber(value.getPvalues()[i]);
                jgen.writeNumber(value.getMacs()[i]);
                jgen.writeNumber(value.getMafs()[i]);
                if (hasGVE) {
                    jgen.writeNumber(value.getGVEs()[i]);
                }
                else {
                    jgen.writeString("N/A");
                }
            jgen.writeEndArray();
        }
    }
}
