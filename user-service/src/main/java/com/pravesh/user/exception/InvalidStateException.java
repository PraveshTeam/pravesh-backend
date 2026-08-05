package com.pravesh.user.exception;

public class InvalidStateException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1295724753993386442L;

	public InvalidStateException(String message) {
        super(message);
    }
}