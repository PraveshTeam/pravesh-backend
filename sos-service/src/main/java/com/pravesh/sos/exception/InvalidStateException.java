package com.pravesh.sos.exception;

public class InvalidStateException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8100276572848070457L;

	public InvalidStateException(String message) {
        super(message);
    }
}