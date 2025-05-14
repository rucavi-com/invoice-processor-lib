package com.rucavi.invoice.processor.handlers;

import java.io.File;

public interface FileRetrievalStepHandler {
    File retrieveFile(String filePath);
}