package com.andresoft.dicomweb.authorization.oidc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;

@SpringBootTest
@ContextConfiguration(classes = com.andresoft.dicomweb.authorization.oidc.TestConfig.class)
@ActiveProfiles({ "test", "oidctokenauth" })
public class ClientCredentialsTokenProviderTest
{

	@SpyBean
	ClientCredentialsTokenProvider tokenProvider;

	HTTPRequest mockHttpRequest;

	TokenRequest mockTokenRequest;

	@BeforeEach
	void setup()
	{
		tokenProvider.init();

		mockHttpRequest = mock(HTTPRequest.class);

		mockTokenRequest = mock(TokenRequest.class);

		Mockito.doReturn(mockTokenRequest).when(tokenProvider).createTokenRequest(any(URI.class),
				any(ClientAuthentication.class), any(AuthorizationGrant.class));

		Mockito.when(mockTokenRequest.toHTTPRequest()).thenReturn(mockHttpRequest);
	}

	@Test
	void testGetAccessToken() throws IOException, ParseException, URISyntaxException
	{

		HTTPResponse response = new HTTPResponse(200);
		response.setEntityContentType(ContentType.APPLICATION_JSON);
		response.setContent(getMockAccessToken());

		Mockito.when(mockHttpRequest.send()).thenReturn(response);

		Optional<AccessToken> accessToken = tokenProvider.getAccessToken("test", "test");
		assertTrue(accessToken.isPresent());

	}

	@Test
	void testGetAccessTokenFailure() throws IOException, ParseException, URISyntaxException
	{

		HTTPResponse response = new HTTPResponse(401);
		response.setEntityContentType(ContentType.APPLICATION_JSON);
		response.setContent(getMockErrorResponse());

		Mockito.when(mockHttpRequest.send()).thenReturn(response);

		Optional<AccessToken> accessToken = tokenProvider.getAccessToken("test", "test");
		assertTrue(accessToken.isEmpty());

	}

	String getMockErrorResponse()
	{
		//@formatter:off
		return
		"""
		{
		   "error": "invalid_client",
		  "error_description": "Invalid client credentials"
		}
		""";
		//@formatter:on
	}

	String getMockAccessToken()
	{
		//@formatter:off
		return 
		"""
		{
		    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0YWxuWWJ6X1dOeWhxWDNKRHZCVVBGUjJsWEs3QkRxOWRxUEROODZ3VnpjIn0.eyJleHAiOjE2NjQyMjI5NTEsImlhdCI6MTY2NDIyMjY1MSwianRpIjoiNzY1YTMwMTYtZmUzNC00YTAyLTkxY2EtYzBkNzg1NGMyMTU2IiwiaXNzIjoiaHR0cHM6Ly8xOTIuMTY4LjMuMTMyOjg4NDMvcmVhbG1zL2RjbTRjaGUiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNzVhYmQ2YjMtYWExOS00NmY2LTljNmUtNTI0N2JkZDJmMWViIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiY3VybCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJkZWZhdWx0LXJvbGVzLWRjbTRjaGUiLCJ1bWFfYXV0aG9yaXphdGlvbiIsInVzZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJjbGllbnRJZCI6ImN1cmwiLCJjbGllbnRIb3N0IjoiMTkyLjE2OC4zLjEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC1jdXJsIiwiY2xpZW50QWRkcmVzcyI6IjE5Mi4xNjguMy4xIn0.IhqmAjrkuehcRn4v--O5rCbdVDt5lpDkB5QiTz9WKPHOwccPbiOTTNvh3ObwRKeAJyZqSrgBDGAu084u-lQR94f3K23IoGCgLWaKIGZcXSY3qmzONTSTplakqr2RSP7IBzs5Ca3kHCnHGCjAXhFxt5KeonUYd2IM0O-rGFbEQKinljvSG1tLzhtWWIE4H1FHbLW_vB-ExyZFb5TmUMJxAEKNVmW0kJqWSs1V7mQUg6lGrWVC5DI6KFMpPtW9k8f6nDyGKBPDGxtJTbpDZcbmgnHWkiOfpaO7JjbdMJknbWB0doleK2iZ6twFM8uNCtg-4eqO6rZ-fHhyQvjdn2sqyA",
		    "expires_in": 299,
		    "refresh_expires_in": 0,
		    "token_type": "Bearer",
		    "not-before-policy": 0,
		    "scope": "profile email"
		}
		""";
		//@formatter:on
	}

}
