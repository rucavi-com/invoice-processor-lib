package com.rucavi.invoice.processor.handlers;

/**
 * Interface for handling the input filtering step in the invoice processing pipeline.
 *
 * @param <T> the type of the input to be filtered.
 */
public interface InputFilterStepHandler<T> {
    /**
     * Filters the input based on specific criteria.
     *
     * @param input The input to be filtered.
     * @return true if the input passes the filter, false otherwise.
     */
    boolean filter(T input);
}
