package com.qualizeal.exceptions;

public class AuthenticationException extends Exception{
	public AuthenticationException() {
		super("Failed to authenticate the credentials against the system");
	}
}