package com.rucavi.invoice.processor.handlers;

import java.io.File;

public interface FileRetrievalStepHandler<T> {
    File retrieveFile(T input);
}