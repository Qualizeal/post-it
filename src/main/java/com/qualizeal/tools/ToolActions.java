package com.qualizeal.tools;

import com.qualizeal.modal.common.TestConfig;
import com.qualizeal.modal.common.ToolConfig;

import java.util.List;

public interface ToolActions {
	ToolActions setToolConfig(ToolConfig toolConfig);
	ToolActions setTestConfig(List<TestConfig> testConfig);
	ToolActions setRuns(List<String> runIds);
	List<String> getRuns();
	List<String> getTestDetails(String runId);
	void authenticate();
	void updateResults();
}