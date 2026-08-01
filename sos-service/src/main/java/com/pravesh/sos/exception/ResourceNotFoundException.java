package com.pravesh.sos.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7396946320281745609L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}