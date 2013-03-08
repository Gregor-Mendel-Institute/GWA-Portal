package com.gmi.nordborglab.browser.server.data.annotation.nclist;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gmi.nordborglab.browser.server.data.annotation.GeneFeature;
import com.google.common.collect.Lists;



import java.io.IOException;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class NCListItemDeserializer extends StdDeserializer<NCListItem> {


    public NCListItemDeserializer() {
        super(NCListItem.class);
    }

    @Override
    public NCListItem deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
        NCListItem item = new NCListItem();
        int i = 0;
        while (jp.nextToken() != JsonToken.END_ARRAY) {
            if (i == 0) {
                item.setStart(jp.getValueAsLong());
            } else if (i==1) {
                item.setEnd(jp.getValueAsLong());
            }
            else if (i == 2) {
                JsonNode node = jp.readValueAsTree();
                if (node.isObject()) {
                    item.setChunk(node.get("chunk").asLong());
                }
                else {
                    item.setStrand(node.asInt());
                }
            }
            else if (i== 3) {
                item.setName(jp.getText());
                String name = item.getName();

            }
            else if (i==5) {

                jp.nextToken();  //REQUIRED otherwise all tokens are shifted by one properly (version 2.1.4, not necessary in version 1.9.12)
                Iterator<GeneFeature> iterator = jp.readValuesAs(GeneFeature.class);
                item.setGeneFeatures(Lists.newArrayList(iterator));
            }
            else if (i==6) {
                jp.nextToken(); //REQUIRED otherwise all tokens are shifted by one properly (version 2.1.4, not necessary in version 1.9.12)
                Iterator<NCListItem> iterator = jp.readValuesAs(NCListItem.class);
                item.setSubNCList(Lists.newArrayList(iterator));
            }
            i= i+1;
        }
        return item;
    }
}
