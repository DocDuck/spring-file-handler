package com.hackerman.filehandler.exception;

// В любой непонятной ситуации при сохранении файла - брось этот эксепшон!
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
