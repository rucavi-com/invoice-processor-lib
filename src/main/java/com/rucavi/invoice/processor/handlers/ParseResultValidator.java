package com.rucavi.invoice.processor.handlers;

/**
 * Interface for validating the result of a parsing operation.
 *
 * @param <T> The type of the parse result.
 */
public interface ParseResultValidator<T> {
    /**
     * Validates the parse result.
     *
     * @param parseResult The parse result to validate.
     * @return A double representing the validation score,
     * where 1.0 indicates a perfect score, and 0.0 indicates
     * a complete failure.
     */
    double validate(T parseResult);
}
