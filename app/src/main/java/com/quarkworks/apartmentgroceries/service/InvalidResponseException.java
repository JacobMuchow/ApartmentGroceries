package com.quarkworks.apartmentgroceries.service;

/**
 * Created by zhao on 10/26/15.
 */
public class InvalidResponseException extends Exception {
    public InvalidResponseException() {}

    public InvalidResponseException(String message) {
        super(message);
    }
}
