package ru.hse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountTest {

    @Mock
    IServerConnection serverConnection;

    Account account;

    @BeforeEach
    public void init() {
        account = new Account();
        account.serverConnection = serverConnection;
    }

    @Test
    public void callLoginTest() {
        // record
        Long session = 1L;

        ServerResponse successOk = new ServerResponse(ServerResponse.SUCCESS, session);
        ServerResponse successBad = new ServerResponse(ServerResponse.SUCCESS, 1);
        ServerResponse undefinedError = new ServerResponse(ServerResponse.UNDEFINED_ERROR, 1);
        ServerResponse alreadyLogged = new ServerResponse(ServerResponse.ALREADY_LOGGED, 1);
        ServerResponse noUserIncorrectPassword = new ServerResponse(ServerResponse.NO_USER_INCORRECT_PASSWORD, 1);

        when(serverConnection.login(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(successOk)
                .thenReturn(successBad)
                .thenReturn(undefinedError)
                .thenReturn(alreadyLogged)
                .thenReturn(noUserIncorrectPassword);

        LocalOperationResponse successOkCorrect = new LocalOperationResponse(LocalOperationResponse.SUCCEED, 1L);
        LocalOperationResponse successBadCorrect = new LocalOperationResponse(LocalOperationResponse.ENCODING_ERROR, successBad);
        LocalOperationResponse undefinedErrorCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, undefinedError);
        LocalOperationResponse alreadyLoggedCorrect = LocalOperationResponse.ACCOUNT_MANAGER_RESPONSE;
        LocalOperationResponse noUserIncorrectPasswordCorrect = LocalOperationResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE;

        // replay

        LocalOperationResponse successOkCurrent = account.callLogin(serverConnection, "login", "password");
        Long currentSession = account.getActiveSession();
        LocalOperationResponse successBadCurrent = account.callLogin(serverConnection, "login", "password");
        LocalOperationResponse undefinedErrorCurrent = account.callLogin(serverConnection, "login", "password");
        LocalOperationResponse alreadyLoggedCurrent = account.callLogin(serverConnection, "login", "password");
        LocalOperationResponse noUserIncorrectPasswordCurrent = account.callLogin(serverConnection, "login", "password");

        // verify
        assertEquals("login", account.getLogin());
        assertEquals(successOkCorrect.code, successOkCurrent.code);
        assertEquals(successOkCorrect.response, successOkCurrent.response);
        assertEquals(session, currentSession);
        assertEquals(successBadCorrect.code, successBadCurrent.code);
        assertEquals(successBadCorrect.response, successBadCurrent.response);
        assertEquals(undefinedErrorCorrect.code, undefinedErrorCurrent.code);
        assertEquals(undefinedErrorCorrect.response, undefinedErrorCurrent.response);
        assertEquals(alreadyLoggedCorrect, alreadyLoggedCurrent);
        assertEquals(noUserIncorrectPasswordCorrect, noUserIncorrectPasswordCurrent);
    }

    @Test
    public void callLogoutTest() {
        // record

        ServerResponse success = new ServerResponse(ServerResponse.SUCCESS, null);
        ServerResponse notLogged = new ServerResponse(ServerResponse.NOT_LOGGED, null);
        ServerResponse undefinedError = new ServerResponse(ServerResponse.UNDEFINED_ERROR, null);

        when(serverConnection.logout(Mockito.anyLong()))
                .thenReturn(undefinedError)
                .thenReturn(notLogged)
                .thenReturn(success);

        LocalOperationResponse nullSessionCorrect = LocalOperationResponse.INCORRECT_SESSION_RESPONSE;
        LocalOperationResponse successCorrect = LocalOperationResponse.SUCCEED_RESPONSE;
        LocalOperationResponse notLoggedCorrect = LocalOperationResponse.NOT_LOGGED_RESPONSE;
        LocalOperationResponse undefinedErrorCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, undefinedError);

        // replay

        LocalOperationResponse nullSessionCurrent = account.callLogout();
        account.activeSession = 1L;

        LocalOperationResponse undefinedErrorCurrent = account.callLogout();
        LocalOperationResponse notLoggedCurrent = account.callLogout();
        LocalOperationResponse successCurrent = account.callLogout();

        Long session = account.getActiveSession();

        // verify

        assertEquals(nullSessionCorrect, nullSessionCurrent);
        assertEquals(successCorrect, successCurrent);
        assertEquals(notLoggedCorrect, notLoggedCurrent);
        assertEquals(undefinedErrorCorrect.code, undefinedErrorCurrent.code);
        assertEquals(undefinedErrorCorrect.response, undefinedErrorCurrent.response);
        assertNull(session);
    }

    @Test
    public void withdrawTest() {
        // record
        double balance = 1.0;

        ServerResponse successOk = new ServerResponse(ServerResponse.SUCCESS, balance);
        ServerResponse successBad = new ServerResponse(ServerResponse.SUCCESS, 1);
        ServerResponse undefinedError = new ServerResponse(ServerResponse.UNDEFINED_ERROR, 1);
        ServerResponse notLogged = new ServerResponse(ServerResponse.NOT_LOGGED, null);
        ServerResponse notMoneyOk = new ServerResponse(ServerResponse.NO_MONEY, balance);
        ServerResponse notMoneyBad = new ServerResponse(ServerResponse.NO_MONEY, 1);

        when(serverConnection.withdraw(Mockito.anyLong(), Mockito.anyDouble()))
                .thenReturn(successOk)
                .thenReturn(successBad)
                .thenReturn(undefinedError)
                .thenReturn(notLogged)
                .thenReturn(notMoneyOk)
                .thenReturn(notMoneyBad);

        LocalOperationResponse nullSessionCorrect = LocalOperationResponse.INCORRECT_SESSION_RESPONSE;
        LocalOperationResponse successOkCorrect = new LocalOperationResponse(LocalOperationResponse.SUCCEED, balance);
        LocalOperationResponse successBadCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, successBad);
        LocalOperationResponse undefinedErrorCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, undefinedError);
        LocalOperationResponse notLoggedCorrect = LocalOperationResponse.NOT_LOGGED_RESPONSE;
        LocalOperationResponse notMoneyOkCorrect = new LocalOperationResponse(LocalOperationResponse.NO_MONEY, balance);
        LocalOperationResponse notMoneyBadCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, notMoneyBad);

        // replay
        LocalOperationResponse nullSessionCurrent = account.withdraw(balance);
        account.activeSession = 1L;

        LocalOperationResponse successOkCurrent = account.withdraw(balance);
        LocalOperationResponse successBadCurrent = account.withdraw(balance);
        LocalOperationResponse undefinedErrorCurrent = account.withdraw(balance);
        LocalOperationResponse notLoggedCurrent = account.withdraw(balance);
        LocalOperationResponse notMoneyOkCurrent = account.withdraw(balance);
        LocalOperationResponse notMoneyBadCurrent = account.withdraw(balance);


        // verify
        assertEquals(nullSessionCorrect, nullSessionCurrent);

        assertEquals(successOkCorrect.code, successOkCurrent.code);
        assertEquals(successOkCorrect.response, successOkCurrent.response);
        assertEquals(successBadCorrect.code, successBadCurrent.code);
        assertEquals(successBadCorrect.response, successBadCurrent.response);
        assertEquals(undefinedErrorCorrect.code, undefinedErrorCurrent.code);
        assertEquals(undefinedErrorCorrect.response, undefinedErrorCurrent.response);
        assertEquals(notLoggedCorrect, notLoggedCurrent);
        assertEquals(notMoneyOkCorrect.code, notMoneyOkCurrent.code);
        assertEquals(notMoneyOkCorrect.response, notMoneyOkCurrent.response);
        assertEquals(notMoneyBadCorrect.code, notMoneyBadCurrent.code);
        assertEquals(notMoneyBadCorrect.response, notMoneyBadCurrent.response);
    }

    @Test
    public void depositTest() {
        double balance = 1.0;

        ServerResponse successOk = new ServerResponse(ServerResponse.SUCCESS, balance);
        ServerResponse successBad = new ServerResponse(ServerResponse.SUCCESS, 1);
        ServerResponse undefinedError = new ServerResponse(ServerResponse.UNDEFINED_ERROR, 1);
        ServerResponse notLogged = new ServerResponse(ServerResponse.NOT_LOGGED, null);

        when(serverConnection.deposit(Mockito.anyLong(), Mockito.anyDouble()))
                .thenReturn(successOk)
                .thenReturn(successBad)
                .thenReturn(undefinedError)
                .thenReturn(notLogged);

        LocalOperationResponse nullSessionCorrect = LocalOperationResponse.INCORRECT_SESSION_RESPONSE;
        LocalOperationResponse successOkCorrect = new LocalOperationResponse(LocalOperationResponse.SUCCEED, balance);
        LocalOperationResponse successBadCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, successBad);
        LocalOperationResponse undefinedErrorCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, undefinedError);
        LocalOperationResponse notLoggedCorrect = LocalOperationResponse.NOT_LOGGED_RESPONSE;

        // replay
        LocalOperationResponse nullSessionCurrent = account.deposit(balance);
        account.activeSession = 1L;

        LocalOperationResponse successOkCurrent = account.deposit(balance);
        LocalOperationResponse successBadCurrent = account.deposit(balance);
        LocalOperationResponse undefinedErrorCurrent = account.deposit(balance);
        LocalOperationResponse notLoggedCurrent = account.deposit(balance);

        // verify
        assertEquals(nullSessionCorrect, nullSessionCurrent);

        assertEquals(successOkCorrect.code, successOkCurrent.code);
        assertEquals(successOkCorrect.response, successOkCurrent.response);
        assertEquals(successBadCorrect.code, successBadCurrent.code);
        assertEquals(successBadCorrect.response, successBadCurrent.response);
        assertEquals(undefinedErrorCorrect.code, undefinedErrorCurrent.code);
        assertEquals(undefinedErrorCorrect.response, undefinedErrorCurrent.response);
        assertEquals(notLoggedCorrect, notLoggedCurrent);
    }

    @Test
    public void getBalanceTest() {
        double balance = 1.0;

        ServerResponse successOk = new ServerResponse(ServerResponse.SUCCESS, balance);
        ServerResponse successBad = new ServerResponse(ServerResponse.SUCCESS, 1);
        ServerResponse undefinedError = new ServerResponse(ServerResponse.UNDEFINED_ERROR, 1);
        ServerResponse notLogged = new ServerResponse(ServerResponse.NOT_LOGGED, null);

        when(serverConnection.getBalance(Mockito.anyLong()))
                .thenReturn(successOk)
                .thenReturn(successBad)
                .thenReturn(undefinedError)
                .thenReturn(notLogged);

        LocalOperationResponse nullSessionCorrect = LocalOperationResponse.INCORRECT_SESSION_RESPONSE;
        LocalOperationResponse successOkCorrect = new LocalOperationResponse(LocalOperationResponse.SUCCEED, balance);
        LocalOperationResponse successBadCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, successBad);
        LocalOperationResponse undefinedErrorCorrect = new LocalOperationResponse(LocalOperationResponse.UNDEFINED_ERROR, undefinedError);
        LocalOperationResponse notLoggedCorrect = LocalOperationResponse.NOT_LOGGED_RESPONSE;

        // replay
        LocalOperationResponse nullSessionCurrent = account.getBalance();
        account.activeSession = 1L;

        LocalOperationResponse successOkCurrent = account.getBalance();
        LocalOperationResponse successBadCurrent = account.getBalance();
        LocalOperationResponse undefinedErrorCurrent = account.getBalance();
        LocalOperationResponse notLoggedCurrent = account.getBalance();

        // verify
        assertEquals(nullSessionCorrect, nullSessionCurrent);

        assertEquals(successOkCorrect.code, successOkCurrent.code);
        assertEquals(successOkCorrect.response, successOkCurrent.response);
        assertEquals(successBadCorrect.code, successBadCurrent.code);
        assertEquals(successBadCorrect.response, successBadCurrent.response);
        assertEquals(undefinedErrorCorrect.code, undefinedErrorCurrent.code);
        assertEquals(undefinedErrorCorrect.response, undefinedErrorCurrent.response);
        assertEquals(notLoggedCorrect, notLoggedCurrent);
    }
}
