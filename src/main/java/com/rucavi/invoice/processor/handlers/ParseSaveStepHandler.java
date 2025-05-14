package com.rucavi.invoice.processor.handlers;

/**
 * Interface for saving parsed invoice data and notifying success or failure.
 *
 * @param <T> The type of the parsed invoice.
 */
public interface ParseSaveStepHandler<T> {
    /**
     * Saves the parsed invoice data and notifies success.
     *
     * @param parsedInvoice The parsed invoice data to save.
     */
    void saveAndNotifySuccess(T parsedInvoice);

    /**
     * Saves the parsed invoice data and notifies failure.
     *
     * @param parsedInvoice The parsed invoice data to save.
     */
    void saveAndNotifyFailure(T parsedInvoice);
}
