package org.jconomy.commands.transfer;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import org.jconomy.transfer.TransferImporter;

public class TransferImporterParser<C> implements ArgumentParser<C, TransferImporter>,
        BlockingSuggestionProvider.Strings<C> {

    private final List<TransferImporter> importers;

    public TransferImporterParser(List<TransferImporter> importers) {
        this.importers = importers;
    }

    @Override
    public @NonNull ArgumentParseResult<TransferImporter> parse(
            @NonNull CommandContext<@NonNull C> context,
            @NonNull CommandInput input) {
        var name = input.peekString();
        var match = importers.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst();
        if (match.isEmpty()) {
            return ArgumentParseResult.failure(
                    new IllegalArgumentException("Unknown import provider: " + name));
        }
        input.readString();
        return ArgumentParseResult.success(match.get());
    }

    @Override
    public @NonNull List<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> context,
            @NonNull CommandInput input) {
        return importers.stream().map(TransferImporter::getName).toList();
    }
}
