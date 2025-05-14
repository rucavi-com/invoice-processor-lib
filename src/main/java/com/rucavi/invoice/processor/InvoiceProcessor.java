package com.rucavi.invoice.processor;


import com.rucavi.invoice.processor.handlers.FileRetrievalStepHandler;

import java.io.File;

public class InvoiceProcessor {
    private final FileRetrievalStepHandler fileRetrievalStepHandler;

    public InvoiceProcessor(FileRetrievalStepHandler fileRetrievalStepHandler) {
        this.fileRetrievalStepHandler = fileRetrievalStepHandler;
    }

    public void process(String filePath) {
        File file = fileRetrievalStepHandler.retrieveFile(filePath);
    }
}