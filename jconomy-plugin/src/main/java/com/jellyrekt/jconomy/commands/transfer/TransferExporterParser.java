package com.jellyrekt.jconomy.commands.transfer;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import com.jellyrekt.jconomy.transfer.TransferExporter;

public class TransferExporterParser<C> implements ArgumentParser<C, TransferExporter>,
        BlockingSuggestionProvider.Strings<C> {

    private final List<TransferExporter> exporters;

    public TransferExporterParser(List<TransferExporter> exporters) {
        this.exporters = exporters;
    }

    @Override
    public @NonNull ArgumentParseResult<TransferExporter> parse(
            @NonNull CommandContext<@NonNull C> context,
            @NonNull CommandInput input) {
        var name = input.peekString();
        var match = exporters.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst();
        if (match.isEmpty()) {
            return ArgumentParseResult.failure(
                    new IllegalArgumentException("Unknown export provider: " + name));
        }
        input.readString();
        return ArgumentParseResult.success(match.get());
    }

    @Override
    public @NonNull List<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> context,
            @NonNull CommandInput input) {
        return exporters.stream().map(TransferExporter::getName).toList();
    }
}
