package com.qualizeal.parser;

import com.qualizeal.modal.common.AuthType;
import com.qualizeal.modal.common.TestConfig;
import com.qualizeal.modal.common.ToolConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParserTest {
	@Test
	public void shouldBeAbleToParseTheXrayConfigFile() throws FileNotFoundException {
		ToolConfig xrayConfig = new ConfigParser().parse("/Users/arjun/Workspace/utaf/post-it/src/test/resources/xrayConfig.json", ToolConfig.class);
		Assert.assertEquals("Automation identifier is not correct", "customfield_10048", xrayConfig.getAutomationReferenceIdentifier());
		Assert.assertEquals("Automation credential is not correct", AuthType.APIKEY, xrayConfig.getCredential().getAuthType());
	}

	@Test
	public void shouldBeAbleToParseTheResultConfigFile() throws FileNotFoundException {
		TestConfig[] testResults = new ConfigParser().parse("/Users/arjun/Workspace/utaf/post-it/src/test/resources/testResults.json", TestConfig[].class);
		List<TestConfig> results = Arrays.stream(testResults).collect(Collectors.toList());
		Assert.assertEquals("All tests are not retrieved", 4, results.size());
		Assert.assertEquals("Results are not correct", "Pass", results.get(0).getStatus());
	}
}