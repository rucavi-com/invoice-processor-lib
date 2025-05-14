package com.rucavi.invoice.processor.handlers;

public interface InvoiceLoadStepHandler<T> {
    void loadInvoice(T parsedInvoice);
}
