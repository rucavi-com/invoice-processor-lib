package com.rucavi.invoice.processor.handlers;

public interface ParseSaveStepHandler<T> {
    void saveAndNotifySuccess(T parsedInvoice);
    void saveAndNotifyFailure(T parsedInvoice);
}
