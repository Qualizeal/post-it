package com.qualizeal.factory;

import com.qualizeal.exceptions.ToolNotImplemented;
import com.qualizeal.tools.TestRailCloudActions;
import com.qualizeal.tools.ToolActions;
import com.qualizeal.tools.XrayCloudActions;
import com.qualizeal.tools.ADOActions;

public class ToolFactory {

	private ToolFactory() {

	}

	public static ToolActions getToolActions(String tool) {
		switch (tool.toLowerCase()) {
			case "xraycloud":
				return new XrayCloudActions();
			case "testrailcloud":
				return new TestRailCloudActions();
			case "azuredevops":
				return new ADOActions();
			default:
				throw new ToolNotImplemented(tool);
		}
	}
}