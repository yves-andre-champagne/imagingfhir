package com.andresoft.imagingfhir.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties(prefix = "httpbasic.auth", ignoreUnknownFields = false, ignoreInvalidFields = false)
@Profile("httpbasicauth")
public class HttpBasicClientProperties
{

	public HttpBasicClientProperties()
	{
		System.out.println("hello:");
	}

	// @Getter
	// @Setter
	// @Value("${httpbasic.auth.username}")
	private String username;

	// @Getter
	// @Setter
	private String password;

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

}
