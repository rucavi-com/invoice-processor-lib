package com.rucavi.invoice.processor.handlers;

import java.io.File;
import java.util.List;

/**
 * Interface for handling the disposal step in the invoice processing pipeline.
 * This step is responsible for disposing of files and performing any necessary cleanup operations.
 */
public interface DisposeStepHandler {
    /**
     * Disposes of the provided files. Any other dispose operations,
     * such as cleaning up resources, can also be performed here.
     *
     * @param files the list of files to be disposed of.
     */
    void dispose(List<File> files);
}
