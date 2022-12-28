package com.qualizeal.exceptions;

public class NoSuchRunException extends Exception{
	public NoSuchRunException() {
		super("Unable to get the run details from the tool");
	}
}