package com.qualizeal.modal.common;

import lombok.Data;

@Data
public class Credentials {
	private String username;
	private String password;
	private String clientId;
	private String clientSecret;
		private AuthType authType;
}