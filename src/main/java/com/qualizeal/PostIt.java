package com.qualizeal;

import com.qualizeal.factory.ToolFactory;
import com.qualizeal.modal.common.TestConfig;
import com.qualizeal.modal.common.ToolConfig;
import com.qualizeal.parser.ConfigParser;
import com.qualizeal.tools.ToolActions;
import lombok.extern.java.Log;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log
public class PostIt {
	public static void main(String[] args) throws FileNotFoundException {
		if(args.length < 3) {
			log.warning("Please provide all the required configurations");
			System.exit(2);
		}
		new PostIt().post(args);
	}

	private void post(String[] args) throws FileNotFoundException{
		ConfigParser configParser = new ConfigParser();
		ToolConfig toolConfig =  configParser.parse(args[0], ToolConfig.class);
		List<TestConfig> testConfig =  Arrays.stream(configParser.parse(args[1], TestConfig[].class)).collect(Collectors.toList());
		List<String> runIdentifiers = Arrays.stream(args, 2, args.length).collect(Collectors.toList());

		ToolActions toolActions = ToolFactory.getToolActions(toolConfig.getTool());
		toolActions.setToolConfig(toolConfig).setTestConfig(testConfig).setRuns(runIdentifiers);

		toolActions.authenticate();
		toolActions.updateResults();
	}
}