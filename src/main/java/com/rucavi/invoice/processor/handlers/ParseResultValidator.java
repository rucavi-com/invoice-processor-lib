package com.rucavi.invoice.processor.handlers;

public interface ParseResultValidator<T> {
    double validate(T parseResult);
}
