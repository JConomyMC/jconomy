package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.jconomy.balances.BalanceAccess;

class AccountDeleterTests {

    private AccountAccess accountAccess;
    private BalanceAccess balanceAccess;
    private AccountDeleter deleter;

    @BeforeEach
    void setUp() {
        accountAccess = mock(AccountAccess.class);
        balanceAccess = mock(BalanceAccess.class);
        deleter = new AccountDeleter(accountAccess, balanceAccess);
    }

    @Test
    void delete_deletes_account_as_durable_deletion_of_its_balances() {
        var id = UUID.randomUUID();
        var durableDeletion = ArgumentCaptor.forClass(Runnable.class);

        deleter.delete(id);

        verify(balanceAccess).deleteByAccount(eq(id), durableDeletion.capture());
        verify(accountAccess, never()).deleteAccount(any());
        durableDeletion.getValue().run();
        verify(accountAccess).deleteAccount(id);
    }

    @Test
    void delete_propagates_failure() {
        var id = UUID.randomUUID();
        doThrow(new RuntimeException("boom")).when(balanceAccess).deleteByAccount(eq(id), any());

        assertThrows(RuntimeException.class, () -> deleter.delete(id));
    }
}
