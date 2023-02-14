package com.qualizeal.modal.azuredevops;

public class ADOParameters {

    private ADOParameters() {

    }

    public static final String TEST_SUITE ="/_apis/testplan/Plans/%s/suites?api-version=7.0";

    public static final String TEST_CASES = "/_apis/testplan/Plans/%s/suites/17546/TestCase";
    public static final String TEST_RUN = "/_apis/test/runs?api-version=7.1-preview.3";
    public static final String TEST_RESULT = "/_apis/test/runs/%s/results?api-version=7.0";
}