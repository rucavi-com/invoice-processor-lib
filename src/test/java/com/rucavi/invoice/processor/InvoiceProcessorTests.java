package com.rucavi.invoice.processor;

import com.rucavi.invoice.processor.handlers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceProcessorTests {
    @Mock
    private DisposeStepHandler disposer;

    @Mock
    private FileRetrievalStepHandler<String> retriever;

    @Mock
    private InputFilterStepHandler<String> filter;

    @Mock
    private InvoiceLoadStepHandler<StringWrapper> loader;

    @Mock
    private InvoiceParserStepHandler<StringWrapper> parser;

    @Mock
    private ParseRectificationStepHandler<StringWrapper> rectifier;

    @Mock
    private ParseResultValidator<StringWrapper> validator;

    private final double validationThreshold = 0.5;

    @Mock
    private ParseSaveStepHandler<StringWrapper> saver;

    private InvoiceProcessor<String, StringWrapper> processor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        processor = new InvoiceProcessor<String, StringWrapper>(
                filter,
                retriever,
                parser,
                new ParseResultValidator[]{validator}, validationThreshold,
                loader,
                rectifier,
                saver,
                disposer);
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenNoValidatorsAreProvided_IllegalArgumentExceptionIsThrown() {
        Executable fail = () -> new InvoiceProcessor<String, StringWrapper>(
                filter,
                retriever,
                parser,
                new ParseResultValidator[]{}, 1.0,
                loader,
                rectifier,
                saver,
                disposer);

        assertThrows(IllegalArgumentException.class, fail);
    }

    @ParameterizedTest
    @SuppressWarnings("unchecked")
    @ValueSource(doubles = {-0.1, 0.0, 1.1})
    void whenThresholdIsInvalid_IllegalArgumentExceptionIsThrown(double threshold) {
        Executable fail = () -> new InvoiceProcessor<String, StringWrapper>(
                filter,
                retriever,
                parser,
                new ParseResultValidator[]{validator}, threshold,
                loader,
                rectifier,
                saver,
                disposer);

        assertThrows(IllegalArgumentException.class, fail);
    }

    @Test
    void whenFilterReturnsFalse_ReturnWithoutParsing() {
        // Arrange
        when(filter.filter("input")).thenReturn(false);

        // Act
        processor.process("input");

        // Assert
        verifyNoInteractions(retriever, parser, validator, loader, rectifier, saver, disposer);
        verifyNoMoreInteractions(filter);
    }

    @Test
    void whenRetrieverFails_PropagateException() {
        // Arrange
        when(filter.filter("input")).thenReturn(true);
        when(retriever.retrieveFile("input")).thenThrow(new RuntimeException("boom"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> processor.process("input"));
        assertEquals("boom", ex.getMessage());
        verifyNoInteractions(parser, validator, loader, rectifier, saver, disposer);
        verifyNoMoreInteractions(filter, retriever);
    }

    @Test
    void whenParserFails_SaveAndNotifyFailure() {
        // Arrange
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        when(parser.parseInvoice(file)).thenThrow(new RuntimeException("boom"));

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver).saveAndNotifyFailure(file, null);
        verify(disposer).dispose(List.of(file));
        verifyNoInteractions(validator, loader, rectifier);
        verifyNoMoreInteractions(filter, retriever, parser, saver, disposer);
    }

    @Test
    void whenValidatorFails_SaveAndNotifyFailure() {
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        var parsed = new StringWrapper("parsed");
        when(parser.parseInvoice(file)).thenReturn(parsed);
        when(validator.validate(parsed)).thenThrow(new RuntimeException("boom"));

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver).saveAndNotifyFailure(file, new StringWrapper("parsed"));
        verify(disposer).dispose(List.of(file));
        verifyNoInteractions(rectifier);
        verifyNoMoreInteractions(filter, retriever, parser, saver, disposer, validator, loader);
    }

    @Test
    void whenInvoiceLoadFails_SaveAndNotifyFailure() {
        // Arrange
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        var parsed = new StringWrapper("parsed");
        when(parser.parseInvoice(file)).thenReturn(parsed);
        when(validator.validate(parsed)).thenReturn(validationThreshold + 0.1);
        doThrow(new RuntimeException("boom")).when(loader).loadInvoice(parsed);

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver).saveAndNotifyFailure(file, parsed);
        verify(disposer).dispose(List.of(file));
        verifyNoInteractions(rectifier);
        verifyNoMoreInteractions(filter, retriever, parser, saver, disposer, validator, loader);
    }

    @Test
    void whenValidationIsSuccessful_SaveAndNotifySuccess() {
        // Arrange
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        var parsed = new StringWrapper("parsed");
        when(parser.parseInvoice(file)).thenReturn(parsed);
        when(validator.validate(parsed)).thenReturn(validationThreshold + 0.1);
        doNothing().when(loader).loadInvoice(parsed);

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver).saveAndNotifySuccess(parsed);
        verify(disposer).dispose(List.of(file));
        verifyNoInteractions(rectifier);
        verifyNoMoreInteractions(filter, retriever, parser, saver, disposer, validator, loader);
    }

    @Test
    void whenValidatorsReturnBelowThreshold_SaveAndNotifyFailure() {
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        var parsed = new StringWrapper("parsed");
        when(parser.parseInvoice(file)).thenReturn(parsed);
        when(validator.validate(parsed)).thenReturn(validationThreshold - 0.1);
        when(rectifier.rectifyParsedInvoice(parsed)).thenReturn(false);

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver).saveAndNotifyFailure(file, parsed);
        verify(disposer).dispose(List.of(file));
        verify(validator, times(1)).validate(parsed);
        verifyNoInteractions(loader);
        verifyNoMoreInteractions(filter, retriever, parser, saver, disposer, validator, rectifier);
    }

    @Test
    void whenRectifierRectifiesButValidatorStillFails_SaveAndNotifyFailure() {
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        var parsed = new StringWrapper("parsed");
        when(parser.parseInvoice(file)).thenReturn(parsed);
        when(validator.validate(parsed)).thenReturn(validationThreshold - 0.1);
        when(rectifier.rectifyParsedInvoice(parsed)).then((inv) -> {
            inv.getArgument(0, StringWrapper.class).setValue("rectified");
            return true;
        });

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver).saveAndNotifyFailure(file, parsed);
        verify(disposer).dispose(List.of(file));
        verify(validator, times(2)).validate(parsed);
        assertEquals("rectified", parsed.getValue());
        verifyNoInteractions(loader);
        verifyNoMoreInteractions(filter, retriever, parser, saver, disposer, validator, rectifier);
    }

    @Test
    void whenRectifierRectifiesAndValidatorSucceedsButLoaderFails_SaveAndNotifySuccess() {
        // Arrange
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        var parsed = new StringWrapper("parsed");
        when(parser.parseInvoice(file)).thenReturn(parsed);
        when(validator.validate(parsed)).thenAnswer(invocation -> {
            String value = invocation.getArgument(0, StringWrapper.class).getValue();
            return validationThreshold + ("rectified".equals(value) ? 0.1 : -0.1);
        });
        when(rectifier.rectifyParsedInvoice(parsed)).then((inv) -> {
            inv.getArgument(0, StringWrapper.class).setValue("rectified");
            return true;
        });
        doThrow(new RuntimeException("boom")).when(loader).loadInvoice(parsed);

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver, times(1)).saveAndNotifyFailure(file, parsed);
        verify(disposer).dispose(List.of(file));
        verify(validator, times(2)).validate(parsed);
        assertEquals("rectified", parsed.getValue());
        verifyNoMoreInteractions(loader, filter, retriever, parser, saver, disposer, validator, rectifier);
    }

    @Test
    void whenRectifierRectifiesAndValidatorSucceedsAndLoaderSucceeds_SaveAndNotifySuccess() {
        // Arrange
        when(filter.filter("input")).thenReturn(true);
        var file = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file));
        var parsed = new StringWrapper("parsed");
        when(parser.parseInvoice(file)).thenReturn(parsed);
        when(validator.validate(parsed)).thenAnswer(invocation -> {
            String value = invocation.getArgument(0, StringWrapper.class).getValue();
            return validationThreshold + ("rectified".equals(value) ? 0.1 : -0.1);
        });
        when(rectifier.rectifyParsedInvoice(parsed)).then((inv) -> {
            inv.getArgument(0, StringWrapper.class).setValue("rectified");
            return true;
        });
        doNothing().when(loader).loadInvoice(parsed);

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(saver, times(1)).saveAndNotifySuccess(parsed);
        verify(disposer).dispose(List.of(file));
        verify(validator, times(2)).validate(parsed);
        assertEquals("rectified", parsed.getValue());
        verifyNoMoreInteractions(filter, retriever, parser, saver, disposer, validator, rectifier);
    }

    @Test
    void whenFirstFileFails_ThenProcessSecondFile() {
        // Arrange
        when(filter.filter("input")).thenReturn(true);
        var file1 = mock(File.class);
        var file2 = mock(File.class);
        when(retriever.retrieveFile("input")).thenReturn(List.of(file1, file2));
        when(parser.parseInvoice(file1)).thenThrow(new RuntimeException("boom"));
        when(parser.parseInvoice(file2)).thenReturn(new StringWrapper("parsed"));
        when(validator.validate(new StringWrapper("parsed"))).thenReturn(validationThreshold + 0.1);
        doNothing().when(loader).loadInvoice(new StringWrapper("parsed"));

        // Act
        assertDoesNotThrow(() -> processor.process("input"));

        // Assert
        verify(retriever).retrieveFile("input");
        verify(parser).parseInvoice(file1);
        verify(saver).saveAndNotifyFailure(file1, null);

        verify(parser).parseInvoice(file2);
        verify(validator).validate(new StringWrapper("parsed"));
        verify(loader).loadInvoice(new StringWrapper("parsed"));
        verify(saver).saveAndNotifySuccess(new StringWrapper("parsed"));

        verify(disposer).dispose(List.of(file1, file2));

        verifyNoInteractions(rectifier);
        verifyNoMoreInteractions(filter, retriever, parser, validator, loader, saver, disposer);
    }

    @Test
    @SuppressWarnings("unchecked")
    void invoiceProcessorCanBeInstantiatedWithoutThreshold() {
        assertDoesNotThrow(() -> new InvoiceProcessor<String, StringWrapper>(
                filter,
                retriever,
                parser,
                new ParseResultValidator[]{validator},
                loader,
                rectifier,
                saver,
                disposer));
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenFilterIsNull_ThenKeepProcessing() {
        // Arrange
        var invoiceProcessor = new InvoiceProcessor<String, StringWrapper>(
                null,
                retriever,
                parser,
                new ParseResultValidator[]{validator}, validationThreshold,
                loader,
                rectifier,
                saver,
                disposer);
        doThrow(new RuntimeException("boom")).when(retriever).retrieveFile("input");

        // Act
        assertThrows(RuntimeException.class, () -> invoiceProcessor.process("input"));
    }

    private static class StringWrapper {
        private String value;

        public StringWrapper(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StringWrapper that)) return false;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }
}
