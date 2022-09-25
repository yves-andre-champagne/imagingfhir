package com.andresoft.dicomweb.authorization;

import lombok.Getter;

public class AuthorizationCredentials
{
	@Getter
	private String type;

	@Getter
	private String credentials;

	@Getter
	private boolean required = true;

	@Getter
	private AuthorizationCredentialsStatus status;

	public AuthorizationCredentials(String type, String credentials, AuthorizationCredentialsStatus status)
	{
		this.type = type;
		this.credentials = credentials;
		this.status = status;
	}

	public AuthorizationCredentials(String type, String credentials, AuthorizationCredentialsStatus status,
			boolean required)
	{
		this.type = type;
		this.credentials = credentials;
		this.status = status;
		this.required = required;
	}

	public enum AuthorizationCredentialsStatus
	{
		AVAILABLE, ERROR
	}
}
