package org.jconomy.adapters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.economy.EconomyResponse.ResponseType;

class LegacyEconomyAdapterTests {

    private net.milkbowl.vault2.economy.Economy modernEconomy;
    private EconomyResponseMapper responseMapper;
    private PlayerResolver playerResolver;
    private LegacyEconomyAdapter adapter;

    @BeforeEach
    void setUp() {
        modernEconomy = mock(net.milkbowl.vault2.economy.Economy.class);
        responseMapper = mock(EconomyResponseMapper.class);
        playerResolver = mock(PlayerResolver.class);
        adapter = new LegacyEconomyAdapter(modernEconomy, responseMapper, playerResolver);
    }

    @Test
    void getBalance_with_player_name_resolves_player_and_delegates() {
        var playerId = UUID.randomUUID();
        var player = offlinePlayerWithId(playerId);
        when(playerResolver.resolve("Alice")).thenReturn(player);
        when(modernEconomy.getBalance(null, playerId)).thenReturn(BigDecimal.valueOf(50));

        assertEquals(50.0, adapter.getBalance("Alice"));
        verify(playerResolver).resolve("Alice");
    }

    @Test
    void isEnabled_delegates_to_modern_economy() {
        when(modernEconomy.isEnabled()).thenReturn(true);

        assertTrue(adapter.isEnabled());
    }

    @Test
    void getName_delegates_to_modern_economy() {
        when(modernEconomy.getName()).thenReturn("JConomy");

        assertEquals("JConomy", adapter.getName());
    }

    @Test
    void getBalance_with_player_delegates_to_modern_economy() {
        var playerId = UUID.randomUUID();
        var player = offlinePlayerWithId(playerId);
        when(modernEconomy.getBalance(null, playerId)).thenReturn(BigDecimal.valueOf(50));

        assertEquals(50.0, adapter.getBalance(player));
    }

    @Test
    void getBalance_with_player_and_world_delegates_to_modern_economy() {
        var playerId = UUID.randomUUID();
        var player = offlinePlayerWithId(playerId);
        when(modernEconomy.getBalance(null, playerId, "nether")).thenReturn(BigDecimal.valueOf(25));

        assertEquals(25.0, adapter.getBalance(player, "nether"));
    }

    @Test
    void depositPlayer_with_player_delegates_to_modern_economy_and_maps_response() {
        var playerId = UUID.randomUUID();
        var player = offlinePlayerWithId(playerId);
        var modernResponse = new EconomyResponse(BigDecimal.TEN, BigDecimal.valueOf(60), ResponseType.SUCCESS, "");
        var legacyResponse = new net.milkbowl.vault.economy.EconomyResponse(
                10, 60, net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS, "");
        when(modernEconomy.deposit(null, playerId, BigDecimal.valueOf(10))).thenReturn(modernResponse);
        when(responseMapper.getLegacyResponse(modernResponse)).thenReturn(legacyResponse);

        var result = adapter.depositPlayer(player, 10.0);

        assertEquals(legacyResponse, result);
    }

    @Test
    void withdrawPlayer_with_player_delegates_to_modern_economy_and_maps_response() {
        var playerId = UUID.randomUUID();
        var player = offlinePlayerWithId(playerId);
        var modernResponse = new EconomyResponse(BigDecimal.TEN, BigDecimal.valueOf(40), ResponseType.SUCCESS, "");
        var legacyResponse = new net.milkbowl.vault.economy.EconomyResponse(
                10, 40, net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS, "");
        when(modernEconomy.withdraw(null, playerId, BigDecimal.valueOf(10))).thenReturn(modernResponse);
        when(responseMapper.getLegacyResponse(modernResponse)).thenReturn(legacyResponse);

        var result = adapter.withdrawPlayer(player, 10.0);

        assertEquals(legacyResponse, result);
    }

    @Test
    void createPlayerAccount_with_player_delegates_to_modern_economy() {
        var playerId = UUID.randomUUID();
        var player = offlinePlayerWithId(playerId);
        when(modernEconomy.createAccount(playerId, "Alice", true)).thenReturn(true);

        assertTrue(adapter.createPlayerAccount(player));
    }

    private static OfflinePlayer offlinePlayerWithId(UUID id) {
        var player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn("Alice");
        return player;
    }
}
