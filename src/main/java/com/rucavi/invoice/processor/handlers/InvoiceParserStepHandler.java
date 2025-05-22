package com.rucavi.invoice.processor.handlers;

import java.io.File;
import java.util.List;

/**
 * Interface for handling the file parsing step in the invoice processing pipeline.
 *
 * @param <T> The output type for the parsed invoices.
 */
public interface InvoiceParserStepHandler<T> {
    /**
     * Parses the invoices from the given files.
     *
     * @param input The files containing the invoices to be parsed.
     * @return The list of parsed invoice objects.
     */
    List<T> parseInvoice(List<File> input);
}
