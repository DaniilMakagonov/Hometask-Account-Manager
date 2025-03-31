package ru.hse;

public interface IPasswordEncoder {
    String makeSecure(String password) throws OperationFailedException;
}
