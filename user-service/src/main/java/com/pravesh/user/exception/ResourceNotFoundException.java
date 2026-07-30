package com.pravesh.user.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8945832479219718423L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}