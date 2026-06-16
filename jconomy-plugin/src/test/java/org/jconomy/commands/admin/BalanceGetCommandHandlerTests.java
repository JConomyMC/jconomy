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

import org.jconomy.accounts.Account;
import org.jconomy.accounts.AccountAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.config.economy.EconomyConfig;

class BalanceGetCommandHandlerTests {

    private AccountAccess accountAccess;
    private EconomyConfig economyConfig;
    private PlayerResolver playerResolver;
    private CommandSender sender;
    private CommandContext<CommandSender> context;
    private BalanceGetCommandHandler handler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        accountAccess = mock(AccountAccess.class);
        economyConfig = mock(EconomyConfig.class);
        playerResolver = mock(PlayerResolver.class);
        sender = mock(CommandSender.class);
        context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        when(economyConfig.getDefaultWorldName()).thenReturn("world");
        when(economyConfig.getAllCurrencyNames()).thenReturn(Set.of("gold"));
        handler = new BalanceGetCommandHandler(accountAccess, economyConfig, playerResolver);
    }

    @Test
    void execute_sends_balance_to_sender() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("gold");

        var account = new Account(playerId, "world");
        account.setBalance("gold", BigDecimal.valueOf(100));
        when(accountAccess.getByIdAndWorld(playerId, "world")).thenReturn(Optional.of(account));

        handler.execute(context);

        verify(sender).sendMessage(contains("100"));
    }

    @Test
    void execute_sends_player_name_and_currency_in_message() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("gold");

        var account = new Account(playerId, "world");
        account.setBalance("gold", BigDecimal.valueOf(50));
        when(accountAccess.getByIdAndWorld(playerId, "world")).thenReturn(Optional.of(account));

        handler.execute(context);

        verify(sender).sendMessage(contains("Steve"));
        verify(sender).sendMessage(contains("gold"));
    }

    @Test
    void execute_sends_error_and_does_not_query_account_when_player_unknown() {
        when(context.get("player")).thenReturn("Ghost");
        when(context.get("currency")).thenReturn("gold");
        when(playerResolver.resolve("Ghost")).thenThrow(new NoSuchElementException());

        handler.execute(context);

        verify(sender).sendMessage(anyString());
        verify(accountAccess, never()).getByIdAndWorld(any(), any());
    }

    @Test
    void execute_sends_error_and_does_not_query_account_when_currency_unknown() {
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("diamonds");

        handler.execute(context);

        verify(sender).sendMessage(anyString());
        verify(accountAccess, never()).getByIdAndWorld(any(), any());
    }

    @Test
    void execute_sends_zero_balance_when_no_account_found() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(context.get("currency")).thenReturn("gold");
        when(accountAccess.getByIdAndWorld(playerId, "world")).thenReturn(Optional.empty());

        handler.execute(context);

        verify(sender).sendMessage(contains("0"));
    }
}
