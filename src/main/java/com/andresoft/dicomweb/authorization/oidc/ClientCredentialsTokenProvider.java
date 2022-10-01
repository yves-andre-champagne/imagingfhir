package com.andresoft.dicomweb.authorization.oidc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.andresoft.imagingfhir.configuration.OidcTokenProperties;
import com.andresoft.imagingfhir.configuration.SSLProperties;
import com.andresoft.util.SSLUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientCredentialsTokenProvider
{

	@Autowired
	OidcTokenProperties oidcTokenProperties;

	@Autowired
	SSLProperties sslProperties;

	Cache<String, HTTPRequest> httpRequestCache;

	@PostConstruct
	public void init()
	{
		httpRequestCache = CacheBuilder.newBuilder().maximumSize(oidcTokenProperties.getCacheSize()).build();

	}

	/**
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @return
	 * @throws URISyntaxException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws ParseException
	 * @throws IOException
	 */
	public Optional<AccessToken> getAccessToken(String clientId, String clientSecret)
			throws URISyntaxException, ParseException, IOException

	{

		HTTPRequest httpRequest = getHTTPRequest(clientId, clientSecret);

		TokenResponse response = TokenResponse.parse(httpRequest.send());

		if (response.indicatesSuccess())
		{

			AccessTokenResponse successResponse = response.toSuccessResponse();

			// Get the access token
			AccessToken token = successResponse.getTokens().getAccessToken();

			return Optional.of(token);

		}
		else
		{
			// We got an error response...
			TokenErrorResponse errorResponse = response.toErrorResponse();

			log.error(errorResponse.toJSONObject().toJSONString());

			return Optional.empty(); // accessToken;

		}

	}

	TokenRequest createTokenRequest(final URI uri, final ClientAuthentication clientAuth,
			final AuthorizationGrant authzGrant)
	{
		return new TokenRequest(uri, clientAuth, authzGrant);
	}

	HTTPRequest getHTTPRequest(String clientId, String clientSecret) throws URISyntaxException
	{
		var httpRequest = httpRequestCache.getIfPresent(clientId);
		if (httpRequest == null)
		{

			// Construct the client credentials grant
			AuthorizationGrant clientGrant = new ClientCredentialsGrant();

			// The credentials to authenticate the client at the token endpoint
			ClientID id = new ClientID(clientId);
			Secret secret = new Secret(clientSecret);
			ClientAuthentication clientAuth = new ClientSecretBasic(id, secret);

			URI tokenEndpoint = new URI(oidcTokenProperties.getEndpoint());

			// create the token request
			TokenRequest request = createTokenRequest(tokenEndpoint, clientAuth, clientGrant);

			httpRequest = request.toHTTPRequest();
			disableSSLCertsCheckingIfRequired(httpRequest);
			httpRequestCache.put(clientId, httpRequest);
		}
		return httpRequest;
	}

	/**
	 * Disables the SSL certificate checking for new instances of
	 * {@link HttpsURLConnection} This has been created to aid testing on a local
	 * box, not for use on production.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	private void disableSSLCertsCheckingIfRequired(HTTPRequest httpRequest)
	{

		if (sslProperties.isTrustAllCerts())
		{
			Optional<SSLContext> sslContext = SSLUtil.getTrustAllCertsSSLContext();

			if (sslContext.isPresent())
			{

				httpRequest.setSSLSocketFactory(sslContext.get().getSocketFactory());

				httpRequest.setHostnameVerifier(SSLUtil.getHostnameVerifier());
			}
		}
	}

}