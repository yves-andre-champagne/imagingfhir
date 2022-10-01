package com.andresoft.imagingfhir.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.andresoft.dicomweb.QidoRsClient;
import com.andresoft.dicomweb.QueryType;
import com.andresoft.dicomweb.RequestParameters;
import com.andresoft.dicomweb.Response;
import com.andresoft.dicomweb.authorization.AuthorizationCredentials;
import com.andresoft.imagingfhir.service.ImagingStudyService;

@SpringBootTest
@ContextConfiguration(classes = com.andresoft.imagingfhir.service.impl.TestConfig.class)
@ActiveProfiles({ "test", "httpbasicauth" })
public class ImagingStudyServiceImplTest
{

	@SpyBean
	ImagingStudyService imagingStudyService;

	@MockBean
	QidoRsClient qidoRsClient;

	@Test
	void testFindByPatientId() throws Exception
	{

		//@formatter:off
		var studyFileUri = Paths.get(getClass()
					   .getClassLoader()
		               .getResource("dicomweb/study.json")
		               .toURI());
		
		var seriesFileUri = Paths.get(getClass()
				   .getClassLoader()
	               .getResource("dicomweb/series.json")
	               .toURI());
		
		var instancesFileUri = Paths.get(getClass()
				   .getClassLoader()
	               .getResource("dicomweb/instances.json")
	               .toURI());
		
		//@formatter:on
		var studyJson = new String(Files.readAllBytes(studyFileUri));
		var seriesJson = new String(Files.readAllBytes(seriesFileUri));
		var instancesJson = new String(Files.readAllBytes(instancesFileUri));

		Mockito.doAnswer(new Answer<Response>()
		{
			@Override
			public Response answer(InvocationOnMock invocation)
			{
				Object[] args = invocation.getArguments();
				var reqParams = (RequestParameters) args[0];
				Response response = null;
				if (reqParams.getQueryType() == QueryType.STUDY)
				{
					response = new Response(200, studyJson);
				}
				if (reqParams.getQueryType() == QueryType.SERIES)
				{
					response = new Response(200, seriesJson);
				}
				if (reqParams.getQueryType() == QueryType.INSTANCE)
				{
					response = new Response(200, instancesJson);
				}

				return response;
			}
		}).when(qidoRsClient).sendRequest(any(RequestParameters.class), any(AuthorizationCredentials.class));

		var imagingStudy = imagingStudyService.findByPatientId("1450727");
		assertNotNull(imagingStudy);
	}

}
