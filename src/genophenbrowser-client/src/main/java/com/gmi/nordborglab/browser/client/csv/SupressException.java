package com.gmi.nordborglab.browser.client.csv;

import org.gwtsupercsv.cellprocessor.CellProcessorAdaptor;
import org.gwtsupercsv.cellprocessor.ift.CellProcessor;
import org.gwtsupercsv.exception.SuperCsvCellProcessorException;
import org.gwtsupercsv.util.CsvContext;

/**
 * Created by uemit.seren on 5/30/14.
 */
public class SupressException extends CellProcessorAdaptor {

    private SuperCsvCellProcessorException suppressedException;
    private Object value;

    public SupressException(CellProcessor next) {
        super(next);
    }

    public Object execute(Object value, CsvContext context) {
        try {
            // attempt to execute the next processor
            this.value = value;
            return next.execute(value, context);

        } catch (SuperCsvCellProcessorException e) {
            // save the exception
            if (value == null) {
                this.value = "MISSING";
            }
            suppressedException = e;
        } finally {

        }
        // and suppress it (null is written as "")
        return null;
    }


    public SuperCsvCellProcessorException getSuppressedException() {
        return suppressedException;
    }

    public Object getValue() {
        return value;
    }

    public void reset() {
        suppressedException = null;
        value = null;
    }
}
