package com.rucavi.invoice.processor.handlers;

public interface FailedInvoiceBuilder<I, T> {
    T buildForError(I input);
}
