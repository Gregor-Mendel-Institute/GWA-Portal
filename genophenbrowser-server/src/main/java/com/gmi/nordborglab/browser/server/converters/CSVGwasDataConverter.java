package com.gmi.nordborglab.browser.server.converters;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Created by uemit.seren on 14.01.14.
 */
public class CSVGwasDataConverter extends AbstractHttpMessageConverter<GWASData> {
    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.forName("utf-8"));
    public final String[] header = new String[]{"chr", "pos", "score", "maf",
            "mac", "GVE"};


    public CSVGwasDataConverter() {
        super(MEDIA_TYPE);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return GWASData.class.equals(clazz);
    }

    @Override
    protected GWASData readInternal(Class<? extends GWASData> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void writeInternal(GWASData gwasData, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStream out;
        ICsvListWriter writer = null;
        outputMessage.getHeaders().setContentType(MEDIA_TYPE);
        outputMessage.getHeaders().set("Content-Disposition", "attachment; filename=\"" + gwasData.getFilename() + "\"");
        out = outputMessage.getBody();
        gwasData.sortByPosition();
        try {
            writer = new CsvListWriter(new OutputStreamWriter(out), CsvPreference.STANDARD_PREFERENCE);
            writer.writeHeader(header);
            for (ChrGWAData chrData:gwasData.getChrGWASData().values()) {
                char chr = chrData.getChr().charAt(3);
                Object[] row = null;
                boolean hasGVE = chrData.getGVEs() != null;
                for (int i = 0;i<chrData.getPositions().length;i++) {
                    if (hasGVE) {
                        writer.write(chr, chrData.getPositions()[i], chrData.getPvalues()[i], chrData.getMafs()[i], chrData.getMacs()[i], chrData.getGVEs()[i]);
                    }
                    else {
                        writer.write(chr, chrData.getPositions()[i], chrData.getPvalues()[i], chrData.getMafs()[i], chrData.getMacs()[i], "");
                    }
                }
            }
            writer.flush();
        }
        catch (Exception e) {
            logger.error("ConvertToCsvException", e);
        }
        finally {
            if( writer != null ) {
                writer.close();
            }
        }
    }
}
