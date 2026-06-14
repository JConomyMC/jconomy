package org.jconomy.commands.transfer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.transfer.TransferImporter;

class TransferImporterParserTests {

    private TransferImporter importerA;
    private TransferImporter importerB;
    private TransferImporterParser<CommandSender> parser;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        importerA = mock(TransferImporter.class);
        importerB = mock(TransferImporter.class);
        org.mockito.Mockito.when(importerA.getName()).thenReturn("vault");
        org.mockito.Mockito.when(importerB.getName()).thenReturn("essentials");
        parser = new TransferImporterParser<>(List.of(importerA, importerB));
    }

    @Test
    @SuppressWarnings("unchecked")
    void parse_returns_importer_for_known_name() {
        var context = mock(CommandContext.class);
        var input = CommandInput.of("vault");

        var result = parser.parse(context, input);

        assertTrue(result.parsedValue().isPresent());
        assertSame(importerA, result.parsedValue().get());
    }

    @Test
    @SuppressWarnings("unchecked")
    void parse_fails_for_unknown_name() {
        var context = mock(CommandContext.class);
        var input = CommandInput.of("unknown");

        var result = parser.parse(context, input);

        assertTrue(result.failure().isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    void stringSuggestions_returns_all_names() {
        var context = mock(CommandContext.class);
        var input = CommandInput.of("");

        var suggestions = parser.stringSuggestions(context, input);

        assertEquals(List.of("vault", "essentials"), suggestions);
    }
}
