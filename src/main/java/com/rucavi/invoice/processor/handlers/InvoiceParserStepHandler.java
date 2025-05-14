package com.rucavi.invoice.processor.handlers;

import java.io.File;

public interface InvoiceParserStepHandler<T> {
    T parseInvoice(File input);
}
