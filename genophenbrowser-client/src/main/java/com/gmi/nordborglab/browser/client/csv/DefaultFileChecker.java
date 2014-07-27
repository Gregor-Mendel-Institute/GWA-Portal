
package com.gmi.nordborglab.browser.client.csv;

import com.gmi.nordborglab.browser.client.mvp.view.diversity.tools.GWASUploadWizardView;
import com.gmi.nordborglab.browser.client.mvp.widgets.FileUploadWidget;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.gwtsupercsv.cellprocessor.CellProcessorAdaptor;
import org.gwtsupercsv.cellprocessor.ParseChar;
import org.gwtsupercsv.cellprocessor.ParseDouble;
import org.gwtsupercsv.cellprocessor.ParseInt;
import org.gwtsupercsv.cellprocessor.ParseLong;
import org.gwtsupercsv.cellprocessor.constraint.Equals;
import org.gwtsupercsv.cellprocessor.constraint.IsIncludedIn;
import org.gwtsupercsv.cellprocessor.ift.CellProcessor;
import org.gwtsupercsv.io.CsvListReader;
import org.gwtsupercsv.io.ICsvListReader;
import org.gwtsupercsv.prefs.CsvPreference;
import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Set;

/**
 * Created by uemit.seren on 5/30/14.
 */
public class DefaultFileChecker implements FileUploadWidget.FileChecker {

    private final List<String> fileExtensions;
    private final List<String> csvMimeTypes;
    private final Optional<CellProcessor[]> headerCellProcessors;
    private final Optional<CellProcessor[]> contentCellProcessors;
    private final List<String> defaultValues;


    public DefaultFileChecker(List<String> fileExtensions, List<String> csvMimeTypes, CellProcessor[] headerCellProcessors, CellProcessor[] contentCellProcessors) {
        this(fileExtensions, csvMimeTypes, headerCellProcessors, contentCellProcessors, null);
    }

    public DefaultFileChecker(List<String> allowedExtensions, List<String> csvMimeTypes, CellProcessor[] headerCellProcessors, CellProcessor[] contentCellProcessors, List<String> defaultValues) {
        this.fileExtensions = allowedExtensions;
        this.csvMimeTypes = csvMimeTypes;
        /*if (contentCellProcessors.length != headerCellProcessors.length) {
            throw new Exception("You have to specify the same amount of processors for header and first-line");
        } */
        this.defaultValues = defaultValues;
        this.contentCellProcessors = addSuppressionCellProcessor(contentCellProcessors);
        this.headerCellProcessors = addSuppressionCellProcessor(headerCellProcessors);
    }

    private Optional<CellProcessor[]> addSuppressionCellProcessor(CellProcessor[] cellProcessors) {
        Optional<CellProcessor[]> cellProcessorsOptional = Optional.fromNullable(cellProcessors);
        if (!cellProcessorsOptional.isPresent())
            return cellProcessorsOptional;
        CellProcessor[] updatedCellProcessors = new CellProcessor[cellProcessors.length];
        for (int i = 0; i < cellProcessors.length; i++) {
            CellProcessor processor = cellProcessors[i];
            if (!(processor instanceof SupressException)) {
                processor = new SupressException(processor);
            }
            updatedCellProcessors[i] = processor;
        }
        return Optional.of(updatedCellProcessors);
    }

    @Override
    public boolean isValidExtension(String extension) {
        return fileExtensions.contains(extension) || csvMimeTypes.contains(extension);
    }

    @Override
    public boolean canParse(String extension) {
        return csvMimeTypes.contains(extension);
    }

    @Override
    public boolean parse(String content, FileUploadWidget.FileCheckerResult result) {
        CellProcessor[] headerCells = headerCellProcessors.get();
        CellProcessor[] contentCells = contentCellProcessors.get();
        resetSupressionCellProcessors();
        boolean parseError = false;
        List<FileUploadWidget.ParseResult> headerParseResults = Lists.newArrayList();
        List<FileUploadWidget.ParseResult> firstLineParseResults = Lists.newArrayList();
        ICsvListReader reader = null;
        try {
            reader = new CsvListReader(content, CsvPreference.STANDARD_PREFERENCE);
            List<Object> header = reader.read(headerCells);
            List<Object> firstLine = reader.read(contentCells);
            for (CellProcessor processor : headerCells) {
                SupressException cell = (SupressException) processor;
                if (cell.getSuppressedException() != null) {
                    parseError = true;
                }
                headerParseResults.add(getParseResultFromHeader(cell));
            }

            for (CellProcessor processor : contentCells) {
                SupressException cell = (SupressException) processor;
                if (cell.getSuppressedException() != null) {
                    parseError = true;
                }
                firstLineParseResults.add(getParseResultFromFirstLine(cell));
            }
        } catch (Exception e) {
            parseError = true;
            result.setParseErrorMsg(e.getMessage());
        } finally {

        }
        result.setParsedFirstLineResult(firstLineParseResults);
        result.setParsedHeaderResult(headerParseResults);
        result.setHasParseErrors(parseError);
        return parseError;
    }

    @Override
    public String getSupportedFileTypes() {
        return Joiner.on(", ").join(Iterables.concat(csvMimeTypes, fileExtensions));
    }

    @Override
    public List<String> getCSVHeaderFormat() {
        CellProcessor[] cellProcessors = headerCellProcessors.orNull();
        return convertProcessorToStrings(cellProcessors, true);
    }

    @Override
    public List<String> getCSVContentFormat() {
        CellProcessor[] cellProcessors = contentCellProcessors.orNull();
        return convertProcessorToStrings(cellProcessors, false);
    }

    private List<String> convertProcessorToStrings(CellProcessor[] cellProcessors, boolean isHeader) {
        if (cellProcessors == null)
            return null;
        List<String> values = Lists.newArrayList();
        int i = 0;
        String defaultValue = null;
        for (CellProcessor processor : cellProcessors) {
            if (!isHeader && defaultValues != null) {
                defaultValue = defaultValues.get(i);
            }
            StringBuilder builder = new StringBuilder(convertProcessorToString(processor, isHeader, defaultValue));
            values.add(builder.toString());
            i++;
        }
        return values;
    }

    private String convertProcessorToString(CellProcessor cellProcessor, boolean isHeader, String defaultValue) {
        boolean isOptional = false;
        boolean isNa = false;
        String value = null;
        if (cellProcessor instanceof CellProcessorAdaptor) {
            CellProcessorAdaptor adaptor = (CellProcessorAdaptor) cellProcessor;
            if (adaptor instanceof org.gwtsupercsv.cellprocessor.Optional) {
                isOptional = true;
            } else {
                value = convertToString(adaptor);
                if (value == "NA") {
                    isNa = true;
                }
            }
            if (value == null || value == "NA") {
                while (adaptor.getNext() != null) {
                    adaptor = (CellProcessorAdaptor) adaptor.getNext();
                    if (adaptor instanceof org.gwtsupercsv.cellprocessor.Optional) {
                        isOptional = true;
                        continue;
                    }
                    value = convertToString(adaptor);
                    if (value == "NA") {
                        isNa = true;
                    } else if (value != null) {
                        break;
                    }
                }
            }
        }
        StringBuilder aditionalTextBuilder = null;
        if (defaultValue != null) {
            aditionalTextBuilder = new StringBuilder(defaultValue);
        } else {
            aditionalTextBuilder = new StringBuilder(value);
        }
        if (!isHeader) {
            if (isNa && isOptional) {
                aditionalTextBuilder.append(" (can be omitted or \"NA\")");
            } else if (isNa) {
                aditionalTextBuilder.append(" (can be \"NA\")");
            } else if (isOptional) {
                aditionalTextBuilder.append(" (can be ommited)");
            }
            value = aditionalTextBuilder.toString();
        }
        return value;
    }

    private String convertToString(CellProcessorAdaptor adaptor) {
        String value = null;
        if (adaptor instanceof Equals) {
            value = ((Equals) adaptor).getConstantValue().toString();
        } else if (adaptor instanceof IsIncludedIn) {
            value = Joiner.on(" | ").join(((IsIncludedIn) adaptor).getPossibleValues());
        } else if (adaptor instanceof ParseDouble) {
            value = "Double";
        } else if (adaptor instanceof ParseInt) {
            value = "Integer";
        } else if (adaptor instanceof GWASUploadWizardView.ParseNAs) {
            value = "NA";
        }
        return value;
    }



    private void resetSupressionCellProcessors() {
        CellProcessor[] headerCells = headerCellProcessors.get();
        CellProcessor[] contentCells = contentCellProcessors.get();
        for (CellProcessor processor : contentCells) {
            SupressException cell = (SupressException) processor;
            cell.reset();
        }

        for (CellProcessor processor : headerCells) {
            SupressException cell = (SupressException) processor;
            cell.reset();
        }
    }

    private FileUploadWidget.ParseResult getParseResultFromHeader(SupressException cell) {
        CellProcessor nextCell = cell;
        String expextedValue = "";
        while (true) {
            if (!(nextCell instanceof CellProcessorAdaptor))
                break;
            nextCell = ((CellProcessorAdaptor) nextCell).getNext();
            if (nextCell instanceof Equals) {
                expextedValue = ((Equals) nextCell).getConstantValue().toString();
                break;
            } else if (nextCell instanceof IsIncludedIn) {
                Set<Object> values = ((IsIncludedIn) nextCell).getPossibleValues();
                expextedValue = Joiner.on("|").join(values);
                break;
            }
        }
        return new FileUploadWidget.ParseResult(cell.getValue().toString(), cell.getSuppressedException() != null, expextedValue);
    }

    public static FileUploadWidget.ParseResult getParseResultFromFirstLine(SupressException cell) {
        CellProcessor nextCell = cell;
        String expextedValue = "";
        String providedValue = null;
        if (cell.getValue() != null)
            providedValue = cell.getValue().toString();
        if (cell.getSuppressedException() == null && providedValue == null) {
            providedValue = "(Optional)";
        }
        while (true) {
            if (!(nextCell instanceof CellProcessorAdaptor))
                break;
            nextCell = ((CellProcessorAdaptor) nextCell).getNext();
            if (nextCell instanceof ParseLong) {
                expextedValue = "Long";
                break;
            } else if (nextCell instanceof ParseDouble) {
                expextedValue = "Double";
                break;
            } else if (nextCell instanceof ParseInt) {
                expextedValue = "Integer";
                break;
            }
        }

        return new FileUploadWidget.ParseResult(providedValue, cell.getSuppressedException() != null, expextedValue);
    }
}
