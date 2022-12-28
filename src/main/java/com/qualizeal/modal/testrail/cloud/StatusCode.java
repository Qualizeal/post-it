package com.qualizeal.modal.testrail.cloud;

import java.util.Arrays;

public enum StatusCode {

	PASS("Pass", 1),
	FAIL("Fail", 5);

	private final String clientCode;
	private final int toolCode;

	StatusCode(String clientCode, int toolCode) {
		this.clientCode = clientCode;
		this.toolCode = toolCode;
	}

	public static StatusCode getByClientCode(String code) {
		return Arrays.stream(StatusCode.values()).filter(s -> s.getClientCode().equalsIgnoreCase(code)).findFirst().orElse(StatusCode.FAIL);
	}

	public String getClientCode() {
		return clientCode;
	}

	public int getToolCode() {
		return toolCode;
	}
}