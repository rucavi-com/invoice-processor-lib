package com.rucavi.invoice.processor;

import com.rucavi.invoice.processor.handlers.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * InvoiceProcessor is a generic class that processes invoices.
 * It retrieves files, parses them, validates the parsed results,
 * rectifies if necessary, and loads the invoices into a system.
 * Finally, it saves the results and notifies success or failure.
 * <p>
 * This class is designed to be flexible and can work with different types of
 * files and parsed invoice objects.
 *
 * @param <I> The type of the input for file retrieval.
 * @param <T> The type of the parsed invoice object.
 */
public class InvoiceProcessor<I, T> {
    private final InputFilterStepHandler<I> inputFilterStepHandler;
    private final FileRetrievalStepHandler<I> fileRetrievalStepHandler;
    private final InvoiceParserStepHandler<T> invoiceParserStepHandler;
    private final ParseResultValidator<T>[] parseResultValidators;
    private final InvoiceLoadStepHandler<T> invoiceLoadStepHandler;
    private final ParseRectificationStepHandler<T> parseRectificationStepHandler;
    private final ParseSaveStepHandler<T> parseSaveStepHandler;
    private final DisposeStepHandler disposeStepHandler;
    private double validationThreshold = 1.0;

    /**
     * Constructor for InvoiceProcessor.
     * <p>
     * The parse result is considered valid if the average validation result is
     * greater than or equal to the validation threshold.
     *
     * @param inputFilterStepHandler        The handler for input filtering.
     * @param fileRetrievalStepHandler      The handler for file retrieval.
     * @param invoiceParserStepHandler      The handler for invoice parsing.
     * @param parseResultValidators         The validators for parsed results. Cannot be empty.
     * @param validationThreshold           The threshold for validation. Must be between 0 and 1.
     * @param invoiceLoadStepHandler        The handler for loading invoices.
     * @param parseRectificationStepHandler The handler for rectifying parsed invoices.
     * @param parseSaveStepHandler          The handler for saving parsed results.
     * @param disposeStepHandler            The handler for disposing of files and resources.
     */
    public InvoiceProcessor(InputFilterStepHandler<I> inputFilterStepHandler,
                            FileRetrievalStepHandler<I> fileRetrievalStepHandler,
                            InvoiceParserStepHandler<T> invoiceParserStepHandler,
                            ParseResultValidator<T>[] parseResultValidators, double validationThreshold,
                            InvoiceLoadStepHandler<T> invoiceLoadStepHandler,
                            ParseRectificationStepHandler<T> parseRectificationStepHandler,
                            ParseSaveStepHandler<T> parseSaveStepHandler,
                            DisposeStepHandler disposeStepHandler) {
        Objects.requireNonNull(fileRetrievalStepHandler, "FileRetrievalStepHandler must be provided");
        Objects.requireNonNull(invoiceParserStepHandler, "InvoiceParserStepHandler must be provided");
        Objects.requireNonNull(parseResultValidators, "ParseResultValidators must be provided");
        Objects.requireNonNull(invoiceLoadStepHandler, "InvoiceLoadStepHandler must be provided");
        Objects.requireNonNull(parseRectificationStepHandler, "ParseRectificationStepHandler must be provided");
        Objects.requireNonNull(parseSaveStepHandler, "ParseSaveStepHandler must be provided");

        validateConstruction(parseResultValidators, validationThreshold);

        this.inputFilterStepHandler = inputFilterStepHandler;
        this.fileRetrievalStepHandler = fileRetrievalStepHandler;
        this.invoiceParserStepHandler = invoiceParserStepHandler;
        this.parseResultValidators = parseResultValidators;
        this.invoiceLoadStepHandler = invoiceLoadStepHandler;
        this.parseRectificationStepHandler = parseRectificationStepHandler;
        this.parseSaveStepHandler = parseSaveStepHandler;
        this.validationThreshold = validationThreshold;
        this.disposeStepHandler = disposeStepHandler;
    }

    /**
     * Constructor for InvoiceProcessor with a default validation threshold of 1.0.
     * <p>
     * The parse result is considered valid if the average validation result is
     * greater than or equal to the validation threshold.
     *
     * @param inputFilterStepHandler        The handler for input filtering.
     * @param fileRetrievalStepHandler      The handler for file retrieval.
     * @param invoiceParserStepHandler      The handler for invoice parsing.
     * @param parseResultValidators         The validators for parsed results. Cannot be empty.
     * @param invoiceLoadStepHandler        The handler for loading invoices.
     * @param parseRectificationStepHandler The handler for rectifying parsed invoices.
     * @param parseSaveStepHandler          The handler for saving parsed results.
     * @param disposeStepHandler            The handler for disposing of files and resources.
     */
    public InvoiceProcessor(InputFilterStepHandler<I> inputFilterStepHandler,
                            FileRetrievalStepHandler<I> fileRetrievalStepHandler,
                            InvoiceParserStepHandler<T> invoiceParserStepHandler,
                            ParseResultValidator<T>[] parseResultValidators,
                            InvoiceLoadStepHandler<T> invoiceLoadStepHandler,
                            ParseRectificationStepHandler<T> parseRectificationStepHandler,
                            ParseSaveStepHandler<T> parseSaveStepHandler,
                            DisposeStepHandler disposeStepHandler) {
        Objects.requireNonNull(fileRetrievalStepHandler, "FileRetrievalStepHandler must be provided");
        Objects.requireNonNull(invoiceParserStepHandler, "InvoiceParserStepHandler must be provided");
        Objects.requireNonNull(parseResultValidators, "ParseResultValidators must be provided");
        Objects.requireNonNull(invoiceLoadStepHandler, "InvoiceLoadStepHandler must be provided");
        Objects.requireNonNull(parseRectificationStepHandler, "ParseRectificationStepHandler must be provided");
        Objects.requireNonNull(parseSaveStepHandler, "ParseSaveStepHandler must be provided");

        validateConstruction(parseResultValidators, validationThreshold);

        this.inputFilterStepHandler = inputFilterStepHandler;
        this.fileRetrievalStepHandler = fileRetrievalStepHandler;
        this.invoiceParserStepHandler = invoiceParserStepHandler;
        this.parseResultValidators = parseResultValidators;
        this.invoiceLoadStepHandler = invoiceLoadStepHandler;
        this.parseRectificationStepHandler = parseRectificationStepHandler;
        this.parseSaveStepHandler = parseSaveStepHandler;
        this.disposeStepHandler = disposeStepHandler;
    }

    /**
     * Processes the input to retrieve, parse, validate, rectify, load, and save the invoice.
     *
     * @param input The input for file retrieval.
     */
    public void process(I input) {
        if (inputFilterStepHandler != null && !inputFilterStepHandler.filter(input)) {
            return;
        }

        List<File> files = fileRetrievalStepHandler.retrieveFile(input);

        files.forEach(rawInvoice -> {
            T parsedInvoice = null;
            boolean isValid = false;

            try {
                parsedInvoice = invoiceParserStepHandler.parseInvoice(rawInvoice);
                isValid = isValidResult(parsedInvoice);
                if (isValid) {
                    invoiceLoadStepHandler.loadInvoice(parsedInvoice);
                } else {
                    boolean rectified = parseRectificationStepHandler.rectifyParsedInvoice(parsedInvoice);
                    if (rectified && isValidResult(parsedInvoice)) {
                        invoiceLoadStepHandler.loadInvoice(parsedInvoice);
                        isValid = true;
                    }
                }
            } catch (Exception e) {
                parseSaveStepHandler.saveAndNotifyFailure(rawInvoice, parsedInvoice);
                return;
            }

            try {
                if (isValid) {
                    parseSaveStepHandler.saveAndNotifySuccess(parsedInvoice);
                } else {
                    parseSaveStepHandler.saveAndNotifyFailure(rawInvoice, parsedInvoice);
                }
            } catch (Exception e) {
                // Avoid failing to process the next file
            }
        });

        if (disposeStepHandler != null) {
            disposeStepHandler.dispose(files);
        }
    }

    private boolean isValidResult(T parsedInvoice) {
        double validationResult = Arrays.stream(parseResultValidators)
                .mapToDouble(validator -> validator.validate(parsedInvoice))
                .average()
                .orElse(0.0);

        return validationResult >= validationThreshold;
    }

    private void validateConstruction(ParseResultValidator<T>[] parseResultValidators, double validationThreshold) {
        if (parseResultValidators.length == 0) {
            throw new IllegalArgumentException("ParseResultValidators must be provided");
        }

        if (validationThreshold <= 0 || validationThreshold > 1) {
            throw new IllegalArgumentException("Validation threshold must be between 0 and 1");
        }
    }
}