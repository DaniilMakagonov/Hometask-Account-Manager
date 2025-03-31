package ru.hse;

public class OperationFailedException extends Exception {
    public LocalOperationResponse response;

    public OperationFailedException(LocalOperationResponse resp) {
        response = resp;
    }
}
