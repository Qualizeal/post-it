package com.qualizeal.exceptions;

public class ToolNotImplemented extends RuntimeException{
	public ToolNotImplemented(String tool) {
		super("Post-It not implemented for " + tool);
	}
}