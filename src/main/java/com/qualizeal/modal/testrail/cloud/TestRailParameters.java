package com.qualizeal.modal.testrail.cloud;

public class TestRailParameters {

	private TestRailParameters() {

	}

	public static final String TESTS_BY_RUN = "/index.php?/api/v2/get_tests/%s";
	public static final String ADD_RESULTS_TO_RUN = "/index.php?/api/v2/add_results/%s";
}