package com.qualizeal.tools;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.qualizeal.modal.common.AuthType;
import com.qualizeal.modal.common.TestConfig;
import com.qualizeal.modal.common.ToolConfig;
import com.qualizeal.modal.testrail.cloud.StatusCode;
import com.qualizeal.modal.testrail.cloud.TestRailDetails;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.qualizeal.modal.testrail.cloud.TestRailParameters.ADD_RESULTS_TO_RUN;
import static com.qualizeal.modal.testrail.cloud.TestRailParameters.TESTS_BY_RUN;

@Log
public class TestRailCloudActions implements ToolActions {

	private ToolConfig toolConfig;
	private List<TestConfig> testConfig;
	@Getter
	private List<String> runs;
	private String authToken;

	Supplier<HttpClient> httpClient = HttpClient::newHttpClient;

	@Override
	public ToolActions setToolConfig(ToolConfig toolConfig) {
		this.toolConfig = toolConfig;
		return this;
	}

	@Override
	public ToolActions setTestConfig(List<TestConfig> testConfig) {
		this.testConfig = testConfig;
		return this;
	}

	@Override
	public ToolActions setRuns(List<String> runIds) {
		this.runs = runIds;
		return this;
	}

	@Override
	public List<String> getTestDetails(String runId) {
		List<TestRailDetails> testDetails = getTestExecutions(runId);
		return testDetails.stream().map(TestRailDetails::getAutomationReference).collect(Collectors.toList());
	}

	@Override
	public void authenticate() {;
		if (AuthType.BASIC == toolConfig.getCredential().getAuthType()) {
			String usernamePassword = toolConfig.getCredential().getUsername() + ":" + toolConfig.getCredential().getPassword();
			authToken = "Basic " + Base64.getEncoder().encodeToString(usernamePassword.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Override
	public void updateResults() {
		Map<String, String> activeRuns = testConfig.stream().collect(Collectors.toMap(TestConfig::getName, TestConfig::getStatus));
		runs.forEach(r -> {
			//instantiate the bulk
			JsonArray jsonRunArray = new JsonArray();
			List<TestRailDetails> testRailTests = getTestExecutions(r);
			for(TestRailDetails t : testRailTests) {
				if(activeRuns.get(t.getAutomationReference()) != null) {
					StatusCode statusCode = StatusCode.getByClientCode(activeRuns.get(t.getAutomationReference()));
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("status_id", statusCode.getToolCode());
					jsonObject.addProperty("test_id", t.getId());
					jsonRunArray.add(jsonObject);
				}
			}
			String runResults = "" +
					"{" +
					"	 \"results\": " + jsonRunArray +
					"}";
			log.info(runResults);

			String serviceEndPoint = String.format(ADD_RESULTS_TO_RUN, r);
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(toolConfig.getEndPoint() + serviceEndPoint))
					.header("Authorization", authToken)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(runResults))
					.build();

			try {
				httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
			} catch (IOException | InterruptedException e) {
				log.warning(e.getMessage());
			}
		});
	}

	@SneakyThrows
	private List<TestRailDetails> getTestExecutions(String runId) {
		String serviceEndPoint = String.format(TESTS_BY_RUN, runId);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(toolConfig.getEndPoint() + serviceEndPoint))
				.header("Authorization", authToken)
				.GET()
				.build();

		HttpResponse<String> response = httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
		List resultsJson = JsonPath.parse(response.body()).read("$.tests");
		Map<String, Object>[] results = new Gson().fromJson(resultsJson.toString(), Map[].class);
		return Arrays.stream(results).map(r -> {
			TestRailDetails testRailDetails = new TestRailDetails();
			testRailDetails.setId(((Double) r.get("id")).intValue());
			testRailDetails.setAutomationReference(((String) r.get(toolConfig.getAutomationReferenceIdentifier())));
			return testRailDetails;
		}).collect(Collectors.toList());
	}
}