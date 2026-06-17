package org.jconomy.commands.admin;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.adapters.PlayerResolver;

class AccountCreateCommandHandlerTests {

    private AccountAccess accountAccess;
    private PlayerResolver playerResolver;
    private CommandSender sender;
    private CommandContext<CommandSender> context;
    private AccountCreateCommandHandler handler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        accountAccess = mock(AccountAccess.class);
        playerResolver = mock(PlayerResolver.class);
        sender = mock(CommandSender.class);
        context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        handler = new AccountCreateCommandHandler(accountAccess, playerResolver);
    }

    @Test
    void execute_creates_account_and_confirms_to_sender() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("Steve");
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(accountAccess.createAccount(playerId, "Steve")).thenReturn(true);

        handler.execute(context);

        verify(accountAccess).createAccount(playerId, "Steve");
        verify(sender).sendMessage(contains("Steve"));
    }

    @Test
    void execute_sends_message_when_account_already_exists() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("Steve");
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");
        when(accountAccess.createAccount(playerId, "Steve")).thenReturn(false);

        handler.execute(context);

        verify(sender).sendMessage(anyString());
    }

    @Test
    void execute_sends_error_when_player_unknown() {
        when(context.get("player")).thenReturn("Ghost");
        when(playerResolver.resolve("Ghost")).thenThrow(new NoSuchElementException());

        handler.execute(context);

        verify(sender).sendMessage(anyString());
        verify(accountAccess, never()).createAccount(any(), any());
    }
}
