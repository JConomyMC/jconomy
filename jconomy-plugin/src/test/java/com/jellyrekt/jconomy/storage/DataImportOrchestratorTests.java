package com.jellyrekt.jconomy.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataImportOrchestratorTests {

    private DataImporter importer;
    private ImportRunRecord record;
    private DataImportOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        importer = mock(DataImporter.class);
        when(importer.getId()).thenReturn("my-importer");
        record = mock(ImportRunRecord.class);
        orchestrator = new DataImportOrchestrator(List.of(importer), record, Logger.getLogger("test"));
    }

    @Test
    void run_executes_importer_when_not_yet_completed() {
        when(record.isCompleted("my-importer")).thenReturn(false);

        orchestrator.run();

        verify(importer).importData();
    }

    @Test
    void run_marks_completed_after_successful_import() {
        when(record.isCompleted("my-importer")).thenReturn(false);

        orchestrator.run();

        verify(record).markCompleted("my-importer");
    }

    @Test
    void run_skips_importer_when_already_completed() {
        when(record.isCompleted("my-importer")).thenReturn(true);

        orchestrator.run();

        verify(importer, never()).importData();
    }

    @Test
    void run_does_not_mark_completed_when_import_throws() {
        when(record.isCompleted("my-importer")).thenReturn(false);
        doThrow(new RuntimeException("fail")).when(importer).importData();

        orchestrator.run();

        verify(record, never()).markCompleted(any());
    }

    @Test
    void run_absent_key_treated_as_not_completed() {
        when(record.isCompleted("my-importer")).thenReturn(false);

        orchestrator.run();

        verify(importer).importData();
        verify(record).markCompleted("my-importer");
    }
}
