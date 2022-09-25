package com.andresoft.imagingfhir.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Keyword;
import org.dcm4che3.data.Tag;
import org.dcm4che3.json.JSONReader;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ImagingStudy;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesComponent;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudyStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
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
import com.andresoft.imagingfhir.exception.ForbiddenException;
import com.andresoft.imagingfhir.exception.NotFoundException;
import com.andresoft.imagingfhir.exception.UnprocessableRequestException;
import com.andresoft.imagingfhir.service.ImagingStudyService;

import ca.uhn.fhir.context.FhirContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImagingStudyServiceImpl implements ImagingStudyService
{

	@Autowired
	HttpAuthorization httpAuthorization;

	@Autowired
	QidoRsClient qidoClient;

	@Autowired
	FhirContext fhirCtx;

	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

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
			var requestAttributes = new Attributes();
			requestAttributes.setString(Tag.PatientID, ElementDictionary.vrOf(Tag.PatientID, null), patientId);
			var requestParameters = new RequestParameters();
			requestParameters.setQueryAttrs(requestAttributes);
			requestParameters.setReturnAll(true);

			Response response = qidoClient.sendRequest(requestParameters, authorizationCredentials);

			var statusCode = response.getStatusCode();
			switch (statusCode)
			{
			case 404, 204:
				throw new NotFoundException();
			case 200:
				var patientStudyAttributesDatasets = processReponse(response, authorizationCredentials);

				for (StudyAttributesDataset s : patientStudyAttributesDatasets)
				{
					var imagingStudy = toImagingStudy(s);
					imagingStudies.add(imagingStudy);

				}
				return imagingStudies;
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

	private void setStudyIdentifier(ImagingStudy imagingStudy, Attributes studyAttr)
	{

		// set identifier
		Identifier studyInstanceIdentifier = imagingStudy.addIdentifier();
		studyInstanceIdentifier.setSystem("urn:dicom:uid");
		var studyInstanceUid = studyAttr.getString(Tag.StudyInstanceUID);
		studyInstanceIdentifier.setValue("urn:oid:" + studyInstanceUid);

		imagingStudy.setId(studyInstanceUid);
	}

	private void setStudyInstanceAvailability(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		var instanceAvailability = studyAttr.getString(Tag.InstanceAvailability, "UNKNOWN");
		switch (instanceAvailability)
		{
		case "ONLINE":
			imagingStudy.setStatus(ImagingStudyStatus.AVAILABLE);
			break;
		case "UNAVAILABLE":
			imagingStudy.setStatus(ImagingStudyStatus.CANCELLED);
			break;
		case "OFFLINE", "NEARLINE":
			imagingStudy.setStatus(ImagingStudyStatus.REGISTERED);
			break;
		default:
			imagingStudy.setStatus(ImagingStudyStatus.UNKNOWN);

		}
	}

	private void setStudyModality(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		var modalitiesInStudy = studyAttr.getStrings(Tag.ModalitiesInStudy);
		for (String modality : modalitiesInStudy)
		{
			var modalityInStudy = imagingStudy.addModality();
			modalityInStudy.setCode(modality);
		}
	}

	private void setStudySubject(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		// set patient:
		var patient = new Patient();
		var patientname = studyAttr.getString(Tag.PatientName);
		var patientId = studyAttr.getString(Tag.PatientID);
		if (patientname != null)
		{

			var patientHumanName = patient.addName();
			populateHumanName(patientname, patientHumanName);
		}

		if (patientId != null)
		{
			var identifier = patient.addIdentifier();
			identifier.setId(patientId);

		}
		if (patient.hasName() || patient.hasIdentifier())
		{

			imagingStudy.setSubject(new Reference(patient));
		}

	}

	private void setStudyReferringPhysician(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		var referringPhysicianName = studyAttr.getString(Tag.ReferringPhysicianName);
		if (referringPhysicianName != null)
		{

			var practitioner = new Practitioner();
			var practionerHumanName = practitioner.addName();

			populateHumanName(referringPhysicianName, practionerHumanName);

			var referringPhysicianReference = new Reference();

			referringPhysicianReference.setResource(practitioner);

			imagingStudy.setReferrer(referringPhysicianReference);
		}
	}

	private void setStudyNumberOfSeries(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		var numSeries = studyAttr.getInts(Tag.NumberOfStudyRelatedSeries);
		imagingStudy.setNumberOfSeries(numSeries[0]);

	}

	private void setStudyNumberOfInstances(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		var numInstances = studyAttr.getInts(Tag.NumberOfStudyRelatedInstances);
		imagingStudy.setNumberOfInstances(numInstances[0]);

	}

	private void setStudyDescription(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		var studyDescription = studyAttr.getString(Tag.StudyDescription);
		imagingStudy.setDescription(studyDescription);
	}

	private void setStudyStartedDate(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		String studyDate = studyAttr.getString(Tag.StudyDate);
		if (studyDate != null)
		{
			try
			{
				var date = dateFormatter.parse(studyDate);
				imagingStudy.setStarted(date);
			}
			catch (java.text.ParseException e)
			{
				// do nothing
			}

		}
	}

	private void setStudyEndpoint(ImagingStudy imagingStudy, Attributes studyAttr)
	{
		var studyRetrieveUrls = studyAttr.getStrings(Tag.RetrieveURL);
		if (studyRetrieveUrls != null)
		{
			var endPoint = new Endpoint().setAddress(studyRetrieveUrls[0]);
			var endpointReference = imagingStudy.addEndpoint();
			endpointReference.setResource(endPoint);
		}
	}

	private void setSeriesStartedDate(ImagingStudySeriesComponent seriesComponent, Attributes seriesAttr)
	{
		String seriesDate = seriesAttr.getString(Tag.SeriesDate);
		if (seriesDate != null)
		{
			try
			{
				var date = dateFormatter.parse(seriesDate);
				seriesComponent.setStarted(date);
			}
			catch (java.text.ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void setSeriesEndpoint(ImagingStudySeriesComponent seriesComponent, Attributes seriesAttr)
	{
		var seriesRetrieveUrls = seriesAttr.getStrings(Tag.RetrieveURL);
		if (seriesRetrieveUrls != null)
		{
			var endPoint = new Endpoint().setAddress(seriesRetrieveUrls[0]);
			var endpointReference = seriesComponent.addEndpoint();
			endpointReference.setResource(endPoint);
		}
	}

	private void addStudyProcedureCode(ImagingStudy imagingStudy, Attributes seriesAttr)
	{
		var procedureCodeSeq = seriesAttr.getSequence(Tag.ProcedureCodeSequence);
		if (procedureCodeSeq != null)
		{
			var procedureCodeSeqAttr = procedureCodeSeq.get(0);

			var codevalue = procedureCodeSeqAttr.getString(Tag.CodeValue);

			var coding​Scheme​Designator = procedureCodeSeqAttr.getString(Tag.CodingSchemeDesignator);

			var code​Meaning = procedureCodeSeqAttr.getString(Tag.CodeMeaning);

			var procedureCodeConcept = imagingStudy.addProcedureCode();

			procedureCodeConcept.addCoding().setCode(codevalue).setDisplay(Keyword.valueOf(Tag.CodeValue));

			procedureCodeConcept.addCoding().setCode(coding​Scheme​Designator)
					.setDisplay(Keyword.valueOf(Tag.CodingSchemeDesignator));

			procedureCodeConcept.addCoding().setCode(code​Meaning).setDisplay(Keyword.valueOf(Tag.CodeMeaning));

		}
	}

	private void setStudySeries(ImagingStudy imagingStudy, SeriesAttributesDataset seriesAttributeDataSet)
	{
		var seriesComponent = imagingStudy.addSeries();
		var seriesAttr = seriesAttributeDataSet.getSeries();

		seriesComponent.setUid(seriesAttr.getString(Tag.SeriesInstanceUID));

		// series number
		var seriesNumber = seriesAttr.getString(Tag.SeriesNumber);
		seriesComponent.setNumber(Integer.valueOf(seriesNumber));

		// series modality
		String seriesModality = seriesAttr.getString(Tag.Modality);
		if (seriesModality != null)
		{
			seriesComponent.setModality(new Coding().setCode(seriesModality));
		}

		// series description
		String seriesDescription = seriesAttr.getString(Tag.SeriesDescription);
		if (seriesDescription != null)
		{
			seriesComponent.setDescription(seriesDescription);
		}

		// series number of instances
		seriesComponent.setNumberOfInstances(seriesAttributeDataSet.getInstances().size());

		// body part examined
		String bodyPartExamined = seriesAttr.getString(Tag.BodyPartExamined);
		if (bodyPartExamined != null)
		{
			seriesComponent.setBodySite(new Coding().setCode(bodyPartExamined));
		}

		String laterality = seriesAttr.getString(Tag.Laterality);
		if (laterality != null)
		{
			seriesComponent.setLaterality(new Coding().setCode(laterality));
		}

		setSeriesStartedDate(seriesComponent, seriesAttr);

		setSeriesEndpoint(seriesComponent, seriesAttr);

		addStudyProcedureCode(imagingStudy, seriesAttr);

		setSeriesInstances(seriesComponent, seriesAttributeDataSet);
	}

	private void setSeriesInstances(ImagingStudySeriesComponent seriesComponent,
			SeriesAttributesDataset seriesAttributeDataSet)
	{
		seriesAttributeDataSet.getInstances().sort(new Comparator<Attributes>()
		{
			@Override
			public int compare(Attributes lhs, Attributes rhs)
			{
				return Integer.valueOf(lhs.getString(Tag.InstanceNumber))
						.compareTo(Integer.valueOf(rhs.getString(Tag.InstanceNumber)));
			}
		});

		for (Attributes instanceAttr : seriesAttributeDataSet.getInstances())
		{
			var instanceComponent = seriesComponent.addInstance();

			var uid = instanceAttr.getString(Tag.SOPInstanceUID);
			instanceComponent.setUid(uid);

			String sopClass = instanceAttr.getString(Tag.SOPClassUID);
			instanceComponent.setSopClass(new Coding().setCode(sopClass));
			String instanceNumber = instanceAttr.getString(Tag.InstanceNumber);

			instanceComponent.setNumber(Integer.parseInt(instanceNumber));

		}

	}

	private ImagingStudy toImagingStudy(StudyAttributesDataset studyAttributesDataset)
	{

		var imagingStudy = new ImagingStudy();

		var studyAttr = studyAttributesDataset.getStudyAttributes();

		// set identifier
		setStudyIdentifier(imagingStudy, studyAttr);

		// set availability
		setStudyInstanceAvailability(imagingStudy, studyAttr);

		// set modalities in study
		setStudyModality(imagingStudy, studyAttr);

		// set patient:
		setStudySubject(imagingStudy, studyAttr);

		// set referring physician
		setStudyReferringPhysician(imagingStudy, studyAttr);

		setStudyNumberOfSeries(imagingStudy, studyAttr);

		setStudyNumberOfInstances(imagingStudy, studyAttr);

		setStudyDescription(imagingStudy, studyAttr);

		setStudyStartedDate(imagingStudy, studyAttr);

		setStudyEndpoint(imagingStudy, studyAttr);

		// set series
		for (SeriesAttributesDataset seriesAttributeDataSet : studyAttributesDataset.getSeriesAttributesDataset())
		{
			setStudySeries(imagingStudy, seriesAttributeDataSet);

		}
		return imagingStudy;

	}

	private void populateHumanName(String personName, HumanName humanName)
	{
		var nameComponents = personName.split("\\^");

		if (nameComponents.length == 1)
		{
			humanName.setFamily(nameComponents[0]);
		}
		else if (nameComponents.length > 1)
		{
			humanName.setFamily(nameComponents[0]);
			humanName.addGiven(nameComponents[1]);
			if (nameComponents.length > 2)
			{
				humanName.addGiven(nameComponents[2]);
			}
			if (nameComponents.length > 3)
			{
				humanName.addPrefix(nameComponents[3]);
			}
			if (nameComponents.length > 4)
			{
				humanName.addSuffix(nameComponents[4]);
			}

		}
	}

}
