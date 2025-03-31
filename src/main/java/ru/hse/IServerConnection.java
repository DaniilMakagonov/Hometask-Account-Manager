package ru.hse;

public interface IServerConnection {
  ServerResponse login(String userName, String mdPass);
  ServerResponse logout(long session);
  ServerResponse withdraw(long session, double balance);
  ServerResponse deposit(long session, double balance);
  ServerResponse getBalance(long session);
}
