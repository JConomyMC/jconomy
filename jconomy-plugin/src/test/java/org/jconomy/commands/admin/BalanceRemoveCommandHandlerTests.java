package org.jconomy.commands.admin;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.accounts.Balance;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.config.economy.EconomyConfig;

class BalanceRemoveCommandHandlerTests {

    private BalanceAccess balanceAccess;
    private EconomyConfig economyConfig;
    private PlayerResolver playerResolver;
    private CommandSender sender;
    private CommandContext<CommandSender> context;
    private BalanceRemoveCommandHandler handler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        balanceAccess = mock(BalanceAccess.class);
        economyConfig = mock(EconomyConfig.class);
        playerResolver = mock(PlayerResolver.class);
        sender = mock(CommandSender.class);
        context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        when(economyConfig.getDefaultWorldName()).thenReturn("world");
        when(economyConfig.getAllCurrencyNames()).thenReturn(Set.of("gold"));
        handler = new BalanceRemoveCommandHandler(balanceAccess, economyConfig, playerResolver);
    }

    @Test
    void execute_subtracts_from_existing_balance_and_confirms_to_sender() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("gold");
        when(context.get("amount")).thenReturn("30");

        var existing = new Balance(playerId, "world", "gold");
        existing.setAmount(BigDecimal.valueOf(100));
        when(balanceAccess.get(playerId, "world", "gold")).thenReturn(Optional.of(existing));

        handler.execute(context);

        verify(balanceAccess).save(argThat(b -> b.getAmount().compareTo(BigDecimal.valueOf(70)) == 0));
        verify(sender).sendMessage(contains("70"));
    }

    @Test
    void execute_subtracts_from_zero_when_no_existing_balance() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("gold");
        when(context.get("amount")).thenReturn("50");
        when(balanceAccess.get(playerId, "world", "gold")).thenReturn(Optional.empty());

        handler.execute(context);

        verify(balanceAccess).save(argThat(b -> b.getAmount().compareTo(BigDecimal.valueOf(-50)) == 0));
    }

    @Test
    void execute_sends_error_when_player_unknown() {
        when(context.get("player")).thenReturn("Ghost");
        when(context.get("currency")).thenReturn("gold");
        when(context.get("amount")).thenReturn("30");
        when(playerResolver.resolve("Ghost")).thenThrow(new NoSuchElementException());

        handler.execute(context);

        verify(sender).sendMessage(anyString());
        verify(balanceAccess, never()).save(any());
    }

    @Test
    void execute_sends_error_when_currency_unknown() {
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("diamonds");
        when(context.get("amount")).thenReturn("30");

        handler.execute(context);

        verify(sender).sendMessage(anyString());
        verify(balanceAccess, never()).save(any());
    }

    @Test
    void execute_sends_error_when_amount_is_invalid() {
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("gold");
        when(context.get("amount")).thenReturn("notanumber");

        handler.execute(context);

        verify(sender).sendMessage(anyString());
        verify(balanceAccess, never()).save(any());
    }
}
