package com.rucavi.invoice.processor;

import com.rucavi.invoice.processor.handlers.*;

import java.io.File;
import java.util.Arrays;


public class InvoiceProcessor<I, T> {
    private final FileRetrievalStepHandler<I> fileRetrievalStepHandler;
    private final InvoiceParserStepHandler<T> invoiceParserStepHandler;
    private final ParseResultValidator<T>[] parseResultValidators;
    private final InvoiceLoadStepHandler<T> invoiceLoadStepHandler;
    private final ParseRectificationStepHandler<T> parseRectificationStepHandler;
    private final ParseSaveStepHandler<T> parseSaveStepHandler;
    private double validationThreshold = 1.0;

    public InvoiceProcessor(FileRetrievalStepHandler<I> fileRetrievalStepHandler,
                            InvoiceParserStepHandler<T> invoiceParserStepHandler,
                            ParseResultValidator<T>[] parseResultValidators, double validationThreshold,
                            InvoiceLoadStepHandler<T> invoiceLoadStepHandler,
                            ParseRectificationStepHandler<T> parseRectificationStepHandler,
                            ParseSaveStepHandler<T> parseSaveStepHandler) {
        validateConstruction(parseResultValidators, validationThreshold);
        this.fileRetrievalStepHandler = fileRetrievalStepHandler;
        this.invoiceParserStepHandler = invoiceParserStepHandler;
        this.parseResultValidators = parseResultValidators;
        this.invoiceLoadStepHandler = invoiceLoadStepHandler;
        this.parseRectificationStepHandler = parseRectificationStepHandler;
        this.parseSaveStepHandler = parseSaveStepHandler;
        this.validationThreshold = validationThreshold;
    }

    public InvoiceProcessor(FileRetrievalStepHandler<I> fileRetrievalStepHandler,
                            InvoiceParserStepHandler<T> invoiceParserStepHandler,
                            ParseResultValidator<T>[] parseResultValidators,
                            InvoiceLoadStepHandler<T> invoiceLoadStepHandler,
                            ParseRectificationStepHandler<T> parseRectificationStepHandler,
                            ParseSaveStepHandler<T> parseSaveStepHandler) {
        validateConstruction(parseResultValidators, validationThreshold);
        this.fileRetrievalStepHandler = fileRetrievalStepHandler;
        this.invoiceParserStepHandler = invoiceParserStepHandler;
        this.parseResultValidators = parseResultValidators;
        this.invoiceLoadStepHandler = invoiceLoadStepHandler;
        this.parseRectificationStepHandler = parseRectificationStepHandler;
        this.parseSaveStepHandler = parseSaveStepHandler;
    }

    public void process(I input) {
        File file = fileRetrievalStepHandler.retrieveFile(input);
        T parsedInvoice = invoiceParserStepHandler.parseInvoice(file);

        if (isValidResult(parsedInvoice)) {
            handleValidParseResult(parsedInvoice);
        } else {
            handleInvalidParseResult(parsedInvoice);
        }
    }

    private void handleValidParseResult(T parsedInvoice) {
        invoiceLoadStepHandler.loadInvoice(parsedInvoice);
        parseSaveStepHandler.saveAndNotifySuccess(parsedInvoice);
    }

    private void handleInvalidParseResult(T parsedInvoice) {
        boolean rectified = parseRectificationStepHandler.rectifyParsedInvoice(parsedInvoice);

        if (rectified && isValidResult(parsedInvoice)) {
            handleValidParseResult(parsedInvoice);
            return;
        }

        parseSaveStepHandler.saveAndNotifyFailure(parsedInvoice);
    }

    private boolean isValidResult(T parsedInvoice) {
        double validationResult = Arrays.stream(parseResultValidators)
                .mapToDouble(validator -> validator.validate(parsedInvoice))
                .average()
                .orElse(0.0);

        return validationResult >= validationThreshold;
    }

    private void validateConstruction(ParseResultValidator<T>[] parseResultValidators, double validationThreshold) {
        if (parseResultValidators == null || parseResultValidators.length == 0) {
            throw new IllegalArgumentException("ParseResultValidators must be provided");
        }

        if (validationThreshold <= 0 || validationThreshold > 1) {
            throw new IllegalArgumentException("Validation threshold must be between 0 and 1");
        }
    }
}