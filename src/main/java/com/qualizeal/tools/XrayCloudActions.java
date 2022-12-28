package com.qualizeal.tools;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.qualizeal.exceptions.AuthenticationException;
import com.qualizeal.exceptions.NoSuchRunException;
import com.qualizeal.modal.common.AuthType;
import com.qualizeal.modal.common.TestConfig;
import com.qualizeal.modal.common.ToolConfig;
import com.qualizeal.modal.xray.cloud.RunResults;
import com.qualizeal.modal.xray.cloud.StatusCode;
import com.qualizeal.modal.xray.cloud.XrayDetails;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.qualizeal.modal.xray.cloud.XrayParameters.*;

@Log
public class XrayCloudActions implements ToolActions {
	private ToolConfig toolConfig;
	private List<TestConfig> testConfig;
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

	@SneakyThrows
	@Override
	public void authenticate() {
		JsonObject authObject = new JsonObject();
		if (AuthType.APIKEY == toolConfig.getCredential().getAuthType()) {
			authObject.addProperty("client_id", toolConfig.getCredential().getClientId());
			authObject.addProperty("client_secret", toolConfig.getCredential().getClientSecret());
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(toolConfig.getEndPoint() + AUTH_END_POINT))
				.POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(authObject)))
				.header("Content-Type", "application/json")
				.build();
		HttpResponse<String> response = httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
		authToken = "Bearer " + response.body().replace("\"", "").trim();
		if (authToken.toLowerCase().contains("error")) {
			throw new AuthenticationException();
		}
		log.info("Token generated successfully - \n" + authToken);
	}

	@SneakyThrows
	@Override
	public void updateResults() {
		String mutation = MUTATION_QUERY;
		Map<String, String> activeRuns = testConfig.stream().collect(Collectors.toMap(TestConfig::getName, TestConfig::getStatus));
		runs.forEach(r -> {
			List<XrayDetails> xrayTests = getTestExecutions(r);
			xrayTests.forEach(x -> {
				if (activeRuns.get(x.getAutomationReference()) != null) {
					//Update results
					StatusCode statusCode = StatusCode.getByClientCode(activeRuns.get(x.getAutomationReference()));
					log.info(String.format(mutation, x.getId(), statusCode.getToolCode()));
					HttpRequest request = gqlRequestBuilder().POST(HttpRequest.BodyPublishers.ofString(String.format(mutation, x.getId(), statusCode.getToolCode()))).build();
					try {
						httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
					} catch (IOException | InterruptedException e) {
						log.warning(e.getMessage());
					}
				}
			});
		});
	}

	@SneakyThrows
	public List<XrayDetails> getTestExecutions(String runId) {
		String query = GQL_QUERY_TEST_EXECUTION;
		log.info(String.format(query, runId, toolConfig.getAutomationReferenceIdentifier()));
		HttpRequest request = gqlRequestBuilder().POST(HttpRequest.BodyPublishers.ofString(String.format(query, runId, toolConfig.getAutomationReferenceIdentifier()))).build();
		HttpResponse<String> response = httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
		List resultsJson = JsonPath.parse(response.body()).read("$.data..testRuns.results");
		if (resultsJson.isEmpty()) throw new NoSuchRunException();
		RunResults[] results = new Gson().fromJson(resultsJson.get(0).toString(), RunResults[].class);
		return Arrays.stream(results).map(r -> {
			XrayDetails details = new XrayDetails();
			details.setId(r.getId());
			details.setIssueId(r.getTestDetails().getIssueId());
			details.setJiraId(r.getTestDetails().getJiraDetails().get("key"));
			details.setAutomationReference(r.getTestDetails().getJiraDetails().get(toolConfig.getAutomationReferenceIdentifier()));
			return details;
		}).collect(Collectors.toList());
	}

	public HttpRequest.Builder gqlRequestBuilder() {
		return HttpRequest.newBuilder()
				.uri(URI.create(toolConfig.getEndPoint() + GRAPHQL_END_POINT))
				.header("Content-Type", "application/json")
				.header("Authorization", authToken);
	}
}