package com.abrar.BOOKSTORE.exception;

/**
 * Thrown when a requested Book / MyBookList entity cannot be found.
 * Replaces the previous behaviour of silently returning null (which caused
 * downstream NullPointerExceptions) or letting a raw
 * EmptyResultDataAccessException escape from delete operations.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}