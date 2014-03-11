package com.jesson.android.internet.core.impl;

import com.jesson.android.internet.core.ResponseBase;
import com.jesson.android.internet.core.json.JsonCreator;
import com.jesson.android.internet.core.json.JsonProperty;

public class JsonErrorResponse extends ResponseBase {

	public int errorCode;

	public String errorMsg;

	@JsonCreator
	public JsonErrorResponse(@JsonProperty("code") int code,
			@JsonProperty("data") String data) {
		this.errorCode = code;
		this.errorMsg = data;
	}
}
