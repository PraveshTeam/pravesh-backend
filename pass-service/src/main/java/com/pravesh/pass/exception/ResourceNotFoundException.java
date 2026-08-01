package com.pravesh.pass.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7379084616337302262L;

	public ResourceNotFoundException(String message) { super(message); }
}