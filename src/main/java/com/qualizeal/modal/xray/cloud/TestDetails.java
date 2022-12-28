package com.qualizeal.modal.xray.cloud;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Map;

@Data
public class TestDetails {
	private String issueId;
	@SerializedName("jira")
	private Map<String, String> jiraDetails;
}