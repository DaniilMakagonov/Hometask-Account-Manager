package ru.hse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScriptedTests {
    @Mock
    IServerConnection serverConnection;

    @Mock
    IPasswordEncoder passwordEncoder;

    AccountManager accountManager;

    @BeforeEach
    public void init() {
        try {
            accountManager = new AccountManager(serverConnection, passwordEncoder);
        } catch (OperationFailedException ignored) {
        }
    }

    @Test
    public void loginAndDepositTest() {
        // record
        double startBalance = 0.0;
        double deposit = 100.0;
        double correctBalance = startBalance + deposit;
        long sessionID = 1L;
        String login = "login";
        String password = "password";
        List<OperationFailedException> exceptions;

        when(serverConnection.login(login, password))
                .thenReturn(new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, null))
                .thenReturn(new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, null))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionID));
        when(serverConnection.getBalance(sessionID))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, startBalance));
        when(serverConnection.deposit(sessionID, deposit))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, correctBalance));

        try {
            when(passwordEncoder.makeSecure(Mockito.anyString()))
                    .thenReturn(password);
        } catch (OperationFailedException ignored) {
        }

        // replay

        Account loginPasswordAccount = accountManager.login(login, password);
        Account passwordAccount = accountManager.login(login, password);
        Account goodAccount = accountManager.login(login, password);

        LocalOperationResponse responseBalance = goodAccount.getBalance();
        LocalOperationResponse responseDeposit = goodAccount.deposit(deposit);

        exceptions = List.copyOf(accountManager.getExceptions());

        // verify
        assertEquals(2, exceptions.size());

        assertNull(loginPasswordAccount);
        assertNull(passwordAccount);

        assertEquals(LocalOperationResponse.SUCCEED, responseBalance.code);
        assertEquals(startBalance, (double) responseBalance.response);

        assertEquals(LocalOperationResponse.SUCCEED, responseDeposit.code);
        assertEquals(correctBalance, (double) responseDeposit.response);

        assertEquals(LocalOperationResponse.NO_USER_INCORRECT_PASSWORD, exceptions.get(0).response.code);
        assertEquals(LocalOperationResponse.NO_USER_INCORRECT_PASSWORD, exceptions.get(1).response.code);
    }

    @Test
    public void withdrawLogoutTest() {
        // record
        long sessionID = 1L;
        double withdraw = 50.0;
        double deposit = 100.0;
        double startBalance = 0.0;
        double endBalance = startBalance + deposit - withdraw;
        String login = "login";
        String password = "password";

        when(serverConnection.login(login, password))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, sessionID));
        when(serverConnection.withdraw(sessionID, withdraw))
                .thenReturn(new ServerResponse(ServerResponse.NO_MONEY, startBalance))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, endBalance));
        when(serverConnection.getBalance(sessionID))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, startBalance));
        when(serverConnection.deposit(sessionID, deposit))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, deposit));
        when(serverConnection.logout(sessionID))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, null));
        try {
            when(passwordEncoder.makeSecure(Mockito.anyString()))
                    .thenReturn(password);
        } catch (OperationFailedException ignored) {
        }

        // replay
        Account account = accountManager.login(login, password);

        LocalOperationResponse badWithDrawResponse = account.withdraw(withdraw);
        LocalOperationResponse balanceResponse = account.getBalance();
        LocalOperationResponse depositResponse = account.deposit(deposit);
        LocalOperationResponse goodWithDrawResponse = account.withdraw(withdraw);

        accountManager.logout(account);

        Account newAccount = accountManager.login(login, password);

        // verify
        assertEquals(LocalOperationResponse.NO_MONEY, badWithDrawResponse.code);
        assertEquals(startBalance, (double) badWithDrawResponse.response);

        assertEquals(LocalOperationResponse.SUCCEED, balanceResponse.code);
        assertEquals(startBalance, (double) balanceResponse.response);

        assertEquals(LocalOperationResponse.SUCCEED, depositResponse.code);
        assertEquals(deposit, (double) depositResponse.response);

        assertEquals(LocalOperationResponse.SUCCEED, goodWithDrawResponse.code);
        assertEquals(endBalance, (double) goodWithDrawResponse.response);

        assertNotNull(newAccount);
    }
}
