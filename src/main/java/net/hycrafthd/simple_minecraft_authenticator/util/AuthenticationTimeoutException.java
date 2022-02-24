package net.hycrafthd.simple_minecraft_authenticator.util;

import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;

public class AuthenticationTimeoutException extends AuthenticationException {
	
	private static final long serialVersionUID = 1L;
	
	public AuthenticationTimeoutException(String message) {
		super(message);
	}
	
	public AuthenticationTimeoutException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
