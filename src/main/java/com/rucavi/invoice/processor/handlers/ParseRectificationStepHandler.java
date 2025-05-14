package com.rucavi.invoice.processor.handlers;

public interface ParseRectificationStepHandler<T> {
    boolean rectifyParsedInvoice(T parsedInvoice);
}
