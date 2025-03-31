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
public class AccountManagerTest {

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
    public void initTest() {
        // arrange
        OperationFailedException alreadyInitiated, nullConnection, nullEncoder, nullConnectionEncoder;

        // act

        // assert
        nullConnection = assertThrows(OperationFailedException.class, () -> accountManager = new AccountManager(null, passwordEncoder));
        nullEncoder = assertThrows(OperationFailedException.class, () -> accountManager = new AccountManager(serverConnection, null));
        nullConnectionEncoder = assertThrows(OperationFailedException.class, () -> accountManager = new AccountManager(null, null));

        assertEquals(LocalOperationResponse.NULL_ARGUMENT, nullConnection.response.code);
        assertEquals(LocalOperationResponse.NULL_ARGUMENT, nullEncoder.response.code);
        assertEquals(LocalOperationResponse.NULL_ARGUMENT, nullConnectionEncoder.response.code);

        assertDoesNotThrow(() -> accountManager = new AccountManager(serverConnection, passwordEncoder));

        alreadyInitiated = assertThrows(OperationFailedException.class, () -> accountManager.init(null, null));
        assertEquals(LocalOperationResponse.ALREADY_INITIATED, alreadyInitiated.response.code);

    }

    @Test
    public void loginTest() {
        // record
        String login = "login", password = "password";

        Account nullLogin;
        Account nullPassword;
        Account nullLoginPassword;
        Account badPasswordAccount;
        Account goodAccount;
        Account repeatedAccount;

        List<OperationFailedException> exceptions;

        when(serverConnection.login(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, 1L));
        try {
            when(passwordEncoder.makeSecure(Mockito.anyString()))
                    .thenThrow(new OperationFailedException(LocalOperationResponse.ENCODING_ERROR_RESPONSE))
                    .thenReturn(password);
        } catch (OperationFailedException ignore) {
        }
        // replay
        nullLogin = accountManager.login(null, password);
        nullPassword = accountManager.login(login, null);
        nullLoginPassword = accountManager.login(null, null);

        badPasswordAccount = accountManager.login(login, password);
        goodAccount = accountManager.login(login, password);
        repeatedAccount = accountManager.login(login, password);

        exceptions = List.copyOf(accountManager.getExceptions());

        // verify
        assertNull(nullLogin);
        assertNull(nullPassword);
        assertNull(nullLoginPassword);

        assertEquals(5, exceptions.size());

        assertEquals(LocalOperationResponse.NULL_ARGUMENT, exceptions.get(0).response.code);
        assertEquals(LocalOperationResponse.NULL_ARGUMENT, exceptions.get(1).response.code);
        assertEquals(LocalOperationResponse.NULL_ARGUMENT, exceptions.get(2).response.code);

        assertNull(badPasswordAccount);
        assertEquals(LocalOperationResponse.ENCODING_ERROR, exceptions.get(3).response.code);

        assertNotNull(goodAccount);

        assertNull(repeatedAccount);
        assertEquals(LocalOperationResponse.ALREADY_INITIATED, exceptions.get(4).response.code);
    }

    @Test
    public void logoutTest() {
        // record
        String login = "login", password = "password";

        List<OperationFailedException> exceptions;


        when(serverConnection.login(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, 1L));

        when(serverConnection.logout(1L))
                .thenReturn(new ServerResponse(ServerResponse.UNDEFINED_ERROR, null))
                .thenReturn(new ServerResponse(ServerResponse.SUCCESS, null));

        try {
            when(passwordEncoder.makeSecure(Mockito.anyString()))
                    .thenReturn(password);
        } catch (OperationFailedException ignore) {
        }

        Account account = accountManager.login(login, password);

        // replay
        accountManager.logout(null);
        accountManager.logout(new Account());

        accountManager.logout(account);
        accountManager.logout(account);
        accountManager.logout(account);

        account = accountManager.login(login, password);

        exceptions = List.copyOf(accountManager.getExceptions());

        // verify
        assertEquals(4, exceptions.size());

        assertEquals(LocalOperationResponse.NULL_ARGUMENT, exceptions.get(0).response.code);
        assertEquals(LocalOperationResponse.NULL_ARGUMENT, exceptions.get(1).response.code);

        assertEquals(LocalOperationResponse.UNDEFINED_ERROR, exceptions.get(2).response.code);

        assertEquals(LocalOperationResponse.INCORRECT_SESSION, exceptions.get(3).response.code);
        assertNotNull(account);
    }
}
