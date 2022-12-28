package com.qualizeal.modal.xray.cloud;

import java.util.Arrays;

public enum StatusCode {

	PASS("Pass", "PASSED"),
	FAIL("Fail", "FAILED");

	private final String clientCode;
	private final String toolCode;

	StatusCode(String clientCode, String toolCode) {
		this.clientCode = clientCode;
		this.toolCode = toolCode;
	}

	public static StatusCode getByClientCode(String code) {
		return Arrays.stream(StatusCode.values()).filter(s -> s.getClientCode().equalsIgnoreCase(code)).findFirst().orElse(StatusCode.FAIL);
	}

	public String getClientCode() {
		return clientCode;
	}

	public String getToolCode() {
		return toolCode;
	}
}