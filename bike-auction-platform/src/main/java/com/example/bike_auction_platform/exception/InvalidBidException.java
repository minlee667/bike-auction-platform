package com.example.bike_auction_platform.exception;

public class InvalidBidException extends RuntimeException {

    public InvalidBidException(String message) {
        super(message);
    }
}