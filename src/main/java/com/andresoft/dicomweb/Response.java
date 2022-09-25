package com.andresoft.dicomweb;

public class Response {

	private int statusCode;
	private String body;

	public Response(int statusCode, String body) {
		super();
		this.statusCode = statusCode;
		this.body = body;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getBody() {
		return body;
	}

}
