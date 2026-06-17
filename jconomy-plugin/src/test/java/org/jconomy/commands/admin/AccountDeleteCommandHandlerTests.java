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
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.adapters.PlayerResolver;

class AccountDeleteCommandHandlerTests {

    private AccountAccess accountAccess;
    private BalanceAccess balanceAccess;
    private PlayerResolver playerResolver;
    private CommandSender sender;
    private CommandContext<CommandSender> context;
    private AccountDeleteCommandHandler handler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        accountAccess = mock(AccountAccess.class);
        balanceAccess = mock(BalanceAccess.class);
        playerResolver = mock(PlayerResolver.class);
        sender = mock(CommandSender.class);
        context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        handler = new AccountDeleteCommandHandler(accountAccess, balanceAccess, playerResolver);
    }

    @Test
    void execute_deletes_account_and_balances_and_confirms_to_sender() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("Steve");
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");

        handler.execute(context);

        verify(accountAccess).deleteAccount(playerId);
        verify(balanceAccess).deleteByAccount(playerId);
        verify(sender).sendMessage(contains("Steve"));
    }

    @Test
    void execute_deletes_balances_before_account() {
        var playerId = UUID.randomUUID();
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("Steve");
        when(playerResolver.resolve("Steve")).thenReturn(player);
        when(context.get("player")).thenReturn("Steve");

        var order = inOrder(balanceAccess, accountAccess);
        handler.execute(context);
        order.verify(balanceAccess).deleteByAccount(playerId);
        order.verify(accountAccess).deleteAccount(playerId);
    }

    @Test
    void execute_sends_error_when_player_unknown() {
        when(context.get("player")).thenReturn("Ghost");
        when(playerResolver.resolve("Ghost")).thenThrow(new NoSuchElementException());

        handler.execute(context);

        verify(sender).sendMessage(anyString());
        verify(accountAccess, never()).deleteAccount(any());
        verify(balanceAccess, never()).deleteByAccount(any());
    }
}
