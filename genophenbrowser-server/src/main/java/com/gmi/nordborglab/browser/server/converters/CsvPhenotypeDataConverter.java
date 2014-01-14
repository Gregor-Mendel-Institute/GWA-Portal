package com.gmi.nordborglab.browser.server.converters;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeValue;
import com.google.common.base.Joiner;
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
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by uemit.seren on 14.01.14.
 */
public class CsvPhenotypeDataConverter extends AbstractHttpMessageConverter<PhenotypeData> {


    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.forName("utf-8"));
    public final String[] header = new String[] { "accession", "value"};


    public CsvPhenotypeDataConverter() {
        super(MEDIA_TYPE);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return PhenotypeData.class.equals(clazz);
    }

    @Override
    protected PhenotypeData readInternal(Class<? extends PhenotypeData> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void writeInternal(PhenotypeData data, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStream out;
        ICsvListWriter writer = null;
        outputMessage.getHeaders().setContentType(MEDIA_TYPE);
        outputMessage.getHeaders().set("Content-Disposition", "attachment; filename=\"" + data.getFilename() + ".csv\"");
        out = outputMessage.getBody();
        try {
            writer = new CsvListWriter(new OutputStreamWriter(out), CsvPreference.STANDARD_PREFERENCE);
            writer.writeHeader(header);
            for (PhenotypeValue value:data.getValues()) {
                writer.write(value.getPassportId(),value.getValue());
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