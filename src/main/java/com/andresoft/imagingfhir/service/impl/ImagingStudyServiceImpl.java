package com.andresoft.imagingfhir.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.stream.JsonParser;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.json.JSONReader;
import org.hl7.fhir.r4.model.ImagingStudy;
import org.springframework.beans.factory.annotation.Autowired;

import com.andresoft.dicomweb.QidoRsClient;
import com.andresoft.dicomweb.QueryType;
import com.andresoft.dicomweb.RequestParameters;
import com.andresoft.dicomweb.Response;
import com.andresoft.dicomweb.SeriesAttributesDataset;
import com.andresoft.dicomweb.StudyAttributesDataset;
import com.andresoft.dicomweb.authorization.AuthorizationCredentials;
import com.andresoft.dicomweb.authorization.AuthorizationCredentials.AuthorizationCredentialsStatus;
import com.andresoft.dicomweb.authorization.HttpAuthorization;
import com.andresoft.imagingfhir.converter.IntancesAttributeConverter;
import com.andresoft.imagingfhir.converter.SeriesAttributesConverter;
import com.andresoft.imagingfhir.converter.StudyAttributesConverter;
import com.andresoft.imagingfhir.exception.ForbiddenException;
import com.andresoft.imagingfhir.exception.NotFoundException;
import com.andresoft.imagingfhir.exception.UnprocessableRequestException;
import com.andresoft.imagingfhir.service.ImagingStudyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImagingStudyServiceImpl implements ImagingStudyService
{

	@Autowired
	private HttpAuthorization httpAuthorization;

	@Autowired
	private QidoRsClient qidoClient;

	private StudyAttributesConverter studyAttributesConverter;
	private SeriesAttributesConverter seriesAttributesConverter;
	private IntancesAttributeConverter intancesAttributeConverter;

	@PostConstruct
	public void init()
	{
		studyAttributesConverter = new StudyAttributesConverter();
		seriesAttributesConverter = new SeriesAttributesConverter();
		intancesAttributeConverter = new IntancesAttributeConverter();
	}

	@Override
	public List<ImagingStudy> findByPatientId(String patientId)
			throws NotFoundException, ForbiddenException, UnprocessableRequestException
	{

		var imagingStudies = new ArrayList<ImagingStudy>();

		var authorizationCredentials = httpAuthorization.getAuthorizationCredentials();
		if (authorizationCredentials.getStatus() == AuthorizationCredentialsStatus.ERROR)
		{
			throw new ForbiddenException();
		}
		try
		{
			var response = sendRequest(patientId, authorizationCredentials);
			var statusCode = response.getStatusCode();
			switch (statusCode)
			{
			case 200:
				var patientStudyAttributesDatasets = processReponse(response, authorizationCredentials);
				for (StudyAttributesDataset s : patientStudyAttributesDatasets)
				{
					var imagingStudy = convertToImagingStudy(s);
					imagingStudies.add(imagingStudy);

				}
				return imagingStudies;

			case 404, 204:
				throw new NotFoundException();

			default:
				log.error("unable to process request: response status code: " + response.getStatusCode() + " - "
						+ response.getBody());

				throw new UnprocessableRequestException("unable to process request");
			}

		}
		catch (URISyntaxException | IOException | InterruptedException e)
		{
			log.error("error processing request", e);
			throw new UnprocessableRequestException(e);
		}
		catch (Exception e)
		{
			log.error("error processing request", e);
			throw new UnprocessableRequestException(e);
		}

	}

	private Response sendRequest(String patientId, AuthorizationCredentials authorizationCredentials)
			throws URISyntaxException, IOException, InterruptedException
	{

		var requestAttributes = new Attributes();
		requestAttributes.setString(Tag.PatientID, ElementDictionary.vrOf(Tag.PatientID, null), patientId);

		var requestParameters = new RequestParameters();
		requestParameters.setQueryAttrs(requestAttributes);
		requestParameters.setReturnAll(true);

		var response = qidoClient.sendRequest(requestParameters, authorizationCredentials);
		return response;

	}

	private List<StudyAttributesDataset> processReponse(Response response,
			AuthorizationCredentials authorizationCredentials)
			throws URISyntaxException, IOException, InterruptedException
	{

		StringReader reader = new StringReader(response.getBody());

		final var attributeDatasets = new ArrayList<Attributes>();

		JsonParser parser = Json.createParser(reader);
		new JSONReader(parser).readDatasets((arg0, arg1) ->
		{
			attributeDatasets.add(arg1);
		});

		var patientStudiesAttributes = new ArrayList<StudyAttributesDataset>();

		for (Attributes studyAttr : attributeDatasets)
		{
			final var studyAttributes = new StudyAttributesDataset();
			studyAttributes.setStudyAttributes(studyAttr);

			var studyInstanceUid = studyAttr.getString(Tag.StudyInstanceUID);
			var seriesDatasets = getSeries(studyInstanceUid, authorizationCredentials);

			var seriesAttributesList = new ArrayList<SeriesAttributesDataset>();

			for (Attributes seriesAttr : seriesDatasets)
			{
				var seriesAttributes = new SeriesAttributesDataset();
				seriesAttributes.setSeries(seriesAttr);

				var seriesInstanceUid = seriesAttr.getString(Tag.SeriesInstanceUID);
				var instanceDatasets = getInstances(studyInstanceUid, seriesInstanceUid, authorizationCredentials);
				seriesAttributes.setInstances(instanceDatasets);

				seriesAttributesList.add(seriesAttributes);
			}
			studyAttributes.setSeriesAttributesDataset(seriesAttributesList);

			patientStudiesAttributes.add(studyAttributes);

		}
		return patientStudiesAttributes;
	}

	private List<Attributes> getSeries(String studyInstanceUid, AuthorizationCredentials authorizationCredentials)
			throws URISyntaxException, IOException, InterruptedException
	{

		var requestParameters = new RequestParameters();

		requestParameters.setQueryType(QueryType.SERIES);
		requestParameters.setStudyInstanceUId(studyInstanceUid);

		requestParameters.setReturnAll(true);
		Response response = qidoClient.sendRequest(requestParameters, authorizationCredentials);

		String body = response.getBody();

		StringReader reader = new StringReader(body);

		final var attributeDatasets = new ArrayList<Attributes>();

		JsonParser parser = Json.createParser(reader);
		new JSONReader(parser).readDatasets((arg0, arg1) ->
		{
			attributeDatasets.add(arg1);
		});

		return attributeDatasets;

	}

	private List<Attributes> getInstances(String studyInstanceUid, String seriesInstanceUid,
			AuthorizationCredentials authorizationCredentials)
			throws URISyntaxException, IOException, InterruptedException
	{

		var requestParameters = new RequestParameters();

		requestParameters.setQueryType(QueryType.INSTANCE);
		requestParameters.setStudyInstanceUId(studyInstanceUid);
		requestParameters.setSeriesInstanceUid(seriesInstanceUid);

		requestParameters.setReturnAll(true);
		Response response = qidoClient.sendRequest(requestParameters, authorizationCredentials);

		String body = response.getBody();

		StringReader reader = new StringReader(body);

		final var attributeDatasets = new ArrayList<Attributes>();

		JsonParser parser = Json.createParser(reader);
		new JSONReader(parser).readDatasets((arg0, arg1) ->
		{
			attributeDatasets.add(arg1);
		});

		return attributeDatasets;

	}

	private ImagingStudy convertToImagingStudy(StudyAttributesDataset studyAttributesDataset)
	{
		var studyAttributes = studyAttributesDataset.getStudyAttributes();
		var imagingStudy = studyAttributesConverter.convert(studyAttributes);

		// set series
		for (SeriesAttributesDataset seriesAttributeDataSet : studyAttributesDataset.getSeriesAttributesDataset())
		{
			var imagingStudySeriesComponent = seriesAttributesConverter.convert(imagingStudy, seriesAttributeDataSet);

			intancesAttributeConverter.convert(imagingStudySeriesComponent, seriesAttributeDataSet.getInstances());
		}
		return imagingStudy;

	}

}
