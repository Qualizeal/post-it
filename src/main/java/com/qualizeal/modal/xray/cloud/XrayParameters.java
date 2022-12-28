package com.qualizeal.modal.xray.cloud;

public class XrayParameters {
	public static final String AUTH_END_POINT = "/api/v2/authenticate";
	public static final String GRAPHQL_END_POINT = "/api/v2/graphql";

	public static final String MUTATION_QUERY = "" +
			"{\"query\": \"mutation {" +
			"    updateTestRunStatus(id: \\\"%s\\\", status: \\\"%s\\\")" +
			"}\"}";

	public static final String GQL_QUERY_TEST_EXECUTION = "" +
			"{ " +
			"    \"query\": " + "\"" +
			"{" +
			"    getTestExecutions(jql: \\\"id = '%s'\\\", limit: 100) {" +
			"        results {" +
			"            testRuns(limit: 100) {" +
			"                results {" +
			"                    id" +
			"                    test {" +
			"                        issueId" +
			"                        jira(fields: [\\\"key\\\", \\\"%s\\\"])" +
			"                    }" +
			"               }" +
			"            }" +
			"        }" +
			"    }" +
			"}" + "\"" +
			"}";

	private XrayParameters() {

	}
}