package com.andresoft.dicomweb.authorization.oidc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.andresoft.dicomweb.authorization.AuthorizationCredentials;
import com.andresoft.dicomweb.authorization.AuthorizationCredentials.AuthorizationCredentialsStatus;
import com.andresoft.dicomweb.authorization.HttpAuthorization;
import com.andresoft.imagingfhir.configuration.OidcClientProperties;
import com.andresoft.imagingfhir.exception.UnprocessableRequestException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OidcTokenAuthorization implements HttpAuthorization
{
	private static String AUTHORIZATION_TYPE = "Bearer";

	@Autowired
	private OidcClientProperties oidcClientProperties;

	@Autowired
	private ClientCredentialsTokenRetriever tokenRetriever;

	@Override
	public AuthorizationCredentials getAuthorizationCredentials()
	{
		AuthorizationCredentials authorizationCredentials = null;
		try
		{
			var accessToken = getRequestAccessToken();
			if (accessToken.isPresent())
			{
				authorizationCredentials = new AuthorizationCredentials(AUTHORIZATION_TYPE,
						accessToken.get().getValue(), AuthorizationCredentialsStatus.AVAILABLE);

			}
		}
		catch (UnprocessableRequestException e)
		{
			authorizationCredentials = new AuthorizationCredentials(AUTHORIZATION_TYPE, "",
					AuthorizationCredentialsStatus.ERROR);
		}
		return authorizationCredentials;
	}

	private Optional<AccessToken> getRequestAccessToken() throws UnprocessableRequestException
	{
		try
		{
			return tokenRetriever.getAccessToken(oidcClientProperties.getId(), oidcClientProperties.getSecret());

		}
		catch (ParseException | URISyntaxException | IOException e)
		{
			log.error("unable to process get token request", e);
			throw new UnprocessableRequestException(e);
		}
	}

}
