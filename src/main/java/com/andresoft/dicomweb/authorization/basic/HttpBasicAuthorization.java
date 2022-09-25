package com.andresoft.dicomweb.authorization.basic;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;

import com.andresoft.dicomweb.authorization.AuthorizationCredentials;
import com.andresoft.dicomweb.authorization.AuthorizationCredentials.AuthorizationCredentialsStatus;
import com.andresoft.dicomweb.authorization.HttpAuthorization;
import com.andresoft.imagingfhir.configuration.HttpBasicClientProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpBasicAuthorization implements HttpAuthorization
{
	private static String AUTHORIZATION_TYPE = "Basic";

	@Autowired
	private HttpBasicClientProperties httpBasicClientProperties;

	@Override
	public AuthorizationCredentials getAuthorizationCredentials()
	{
		AuthorizationCredentials authorizationCredentials = null;
		try
		{
			var credentials = httpBasicClientProperties.getUsername() + ":" + httpBasicClientProperties.getPassword();

			var base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

			authorizationCredentials = new AuthorizationCredentials(AUTHORIZATION_TYPE, base64Credentials,
					AuthorizationCredentialsStatus.AVAILABLE);

		}
		catch (Exception e)
		{
			log.error("unable to process get token request", e);
			authorizationCredentials = new AuthorizationCredentials(AUTHORIZATION_TYPE, "",
					AuthorizationCredentialsStatus.ERROR);
		}
		return authorizationCredentials;
	}

}
