package com.rucavi.invoice.processor.handlers;

/**
 * Interface for rectifying the parsed invoice if it is not valid.
 *
 * @param <T> The type of the parse result.
 */
public interface ParseRectificationStepHandler<T> {
    /**
     * This method is called to rectify the parsed invoice if it is not valid.
     *
     * @param parsedInvoice The parsed invoice to be rectified.
     * @return true if the invoice parse result was modified/rectified,
     * false if it was left as-is.
     */
    boolean rectifyParsedInvoice(T parsedInvoice);
}
