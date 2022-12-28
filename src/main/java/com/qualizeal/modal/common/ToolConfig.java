package com.qualizeal.modal.common;

import lombok.Data;

@Data
public class ToolConfig {
	private String tool;
	private Credentials credential;
	private String endPoint;
	private String automationReferenceIdentifier;
}