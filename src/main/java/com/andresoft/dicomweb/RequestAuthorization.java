package com.andresoft.dicomweb;

import lombok.Getter;

public class RequestAuthorization
{

	@Getter
	String type;

	@Getter
	String credentials;

	public RequestAuthorization(String type, String credentials)
	{
		this.type = type;
		this.credentials = credentials;
	}

}
