package com.pravesh.user.exception;

public class DuplicateResourceException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4226559053574962769L;

	public DuplicateResourceException(String message) {
        super(message);
    }
}