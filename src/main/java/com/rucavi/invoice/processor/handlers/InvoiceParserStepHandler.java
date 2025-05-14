package com.rucavi.invoice.processor.handlers;

import java.io.File;

/**
 * Interface for handling the file parsing step in the invoice processing pipeline.
 *
 * @param <T> The output type for the parsed invoice.
 */
public interface InvoiceParserStepHandler<T> {
    /**
     * Parses the invoice from the given file.
     *
     * @param input The file containing the invoice to be parsed.
     * @return The parsed invoice object.
     */
    T parseInvoice(File input);
}
