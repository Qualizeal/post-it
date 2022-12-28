package com.qualizeal.modal.common;

import lombok.Data;

import java.util.Date;

@Data
public class TestConfig {
	private String name;
	private String status;
	private Date startTime;
	private Date endTime;
}