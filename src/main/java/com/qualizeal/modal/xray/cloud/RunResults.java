package com.qualizeal.modal.xray.cloud;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public
class RunResults {
	private String id;
	@SerializedName("test")
	private TestDetails testDetails;
}