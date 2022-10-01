package com.andresoft.dicomweb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.andresoft.dicomweb.authorization.AuthorizationCredentials;
import com.andresoft.dicomweb.authorization.HttpAuthorization;

@SpringBootTest
@ContextConfiguration(classes = com.andresoft.dicomweb.TestConfig.class)
@ActiveProfiles({ "test", "httpbasicauth" })
public class QitoRsClientTest
{

	@SpyBean
	QidoRsClient qidoClient;

	@Autowired
	HttpAuthorization httpAuthorization;

	@SuppressWarnings("unchecked")
	@Test
	void testSendStudyRequest() throws Exception
	{

		//@formatter:off
		var fileUri = Paths.get(getClass()
					   .getClassLoader()
		               .getResource("dicomweb/study.json")
		               .toURI());
		//@formatter:on
		var studyJson = new String(Files.readAllBytes(fileUri));

		HttpClient mockHttpClient = Mockito.mock(HttpClient.class);

		HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);

		Mockito.when(mockHttpResponse.statusCode()).thenReturn(200);

		Mockito.when(mockHttpResponse.body()).thenReturn(studyJson);

		Mockito.when(mockHttpClient.send(any(HttpRequest.class), (BodyHandler<String>) any()))
				.thenReturn(mockHttpResponse);

		Mockito.doReturn(mockHttpClient).when(qidoClient).buildHttpClient();

		qidoClient.init();

		AuthorizationCredentials authorizationCredentials = httpAuthorization.getAuthorizationCredentials();

		var requestAttributes = new Attributes();
		requestAttributes.setString(Tag.PatientID, ElementDictionary.vrOf(Tag.PatientID, null), "123456");
		var requestParameters = new RequestParameters();
		requestParameters.setQueryAttrs(requestAttributes);
		requestParameters.setReturnAll(true);

		Mockito.when(mockHttpResponse.statusCode()).thenReturn(200);

		Mockito.when(mockHttpResponse.body()).thenReturn(studyJson);

		Mockito.when(mockHttpClient.send(any(HttpRequest.class), (BodyHandler<String>) any(BodyHandlers.class)))
				.thenReturn(mockHttpResponse);

		Response response = qidoClient.sendRequest(requestParameters, authorizationCredentials);

		assertEquals(studyJson, response.getBody());

	}
}
