package com.rucavi.invoice.processor.handlers;

import java.io.File;

/**
 * Interface for handling the file retrieval step in the invoice processing pipeline.
 *
 * @param <T> the type of the input to the file retrieval step.
 */
public interface FileRetrievalStepHandler<T> {
    /**
     * Retrieves the invoice raw file based on the provided input.
     *
     * @param input the input used to retrieve the file.
     * @return the retrieved file.
     */
    File retrieveFile(T input);
}