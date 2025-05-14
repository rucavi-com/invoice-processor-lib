package com.rucavi.invoice.processor.handlers;

/**
 * Interface for handling the loading of parsed invoices to the target system.
 *
 * @param <T> The type of the parse result.
 */
public interface InvoiceLoadStepHandler<T> {
    /**
     * Loads the parsed invoice to the target system.
     *
     * @param parsedInvoice The parsed invoice to be loaded.
     */
    void loadInvoice(T parsedInvoice);
}
