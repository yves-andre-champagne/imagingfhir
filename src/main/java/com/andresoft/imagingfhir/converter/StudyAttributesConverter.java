package com.andresoft.imagingfhir.converter;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ImagingStudy;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudyStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StudyAttributesConverter extends BaseDicomWebAttributesConverter
{

	public ImagingStudy convert(Attributes attributes)
	{
		var imagingStudy = new ImagingStudy();
		// set identifier
		setStudyIdentifier(imagingStudy, attributes);

		// set availability
		setStudyInstanceAvailability(imagingStudy, attributes);

		// set modalities in study
		setStudyModality(imagingStudy, attributes);

		// set patient:
		setStudySubject(imagingStudy, attributes);

		// set referring physician
		setStudyReferringPhysician(imagingStudy, attributes);

		setStudyNumberOfSeries(imagingStudy, attributes);

		setStudyNumberOfInstances(imagingStudy, attributes);

		setStudyDescription(imagingStudy, attributes);

		setStudyStartedDate(imagingStudy, attributes);

		setStudyEndpoint(imagingStudy, attributes);

		return imagingStudy;

	}

	private void setStudyIdentifier(ImagingStudy imagingStudy, Attributes attributes)
	{

		// set identifier
		Identifier studyInstanceIdentifier = imagingStudy.addIdentifier();
		studyInstanceIdentifier.setSystem("urn:dicom:uid");
		var studyInstanceUid = attributes.getString(Tag.StudyInstanceUID);
		studyInstanceIdentifier.setValue("urn:oid:" + studyInstanceUid);

		imagingStudy.setId(studyInstanceUid);
	}

	private void setStudyInstanceAvailability(ImagingStudy imagingStudy, Attributes attributes)
	{
		var instanceAvailability = attributes.getString(Tag.InstanceAvailability, "UNKNOWN");
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

	private void setStudyModality(ImagingStudy imagingStudy, Attributes attributes)
	{
		var modalitiesInStudy = attributes.getStrings(Tag.ModalitiesInStudy);
		for (String modality : modalitiesInStudy)
		{
			var modalityInStudy = imagingStudy.addModality();
			modalityInStudy.setCode(modality);
		}
	}

	private void setStudySubject(ImagingStudy imagingStudy, Attributes attributes)
	{
		// set patient:
		var patient = new Patient();
		var patientname = attributes.getString(Tag.PatientName);
		var patientId = attributes.getString(Tag.PatientID);
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

	private void setStudyReferringPhysician(ImagingStudy imagingStudy, Attributes attributes)
	{
		var referringPhysicianName = attributes.getString(Tag.ReferringPhysicianName);
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

	private void setStudyNumberOfSeries(ImagingStudy imagingStudy, Attributes attributes)
	{
		var numSeries = attributes.getInts(Tag.NumberOfStudyRelatedSeries);
		imagingStudy.setNumberOfSeries(numSeries[0]);

	}

	private void setStudyNumberOfInstances(ImagingStudy imagingStudy, Attributes attributes)
	{
		var numInstances = attributes.getInts(Tag.NumberOfStudyRelatedInstances);
		imagingStudy.setNumberOfInstances(numInstances[0]);

	}

	private void setStudyDescription(ImagingStudy imagingStudy, Attributes attributes)
	{
		var studyDescription = attributes.getString(Tag.StudyDescription);
		imagingStudy.setDescription(studyDescription);
	}

	private void setStudyStartedDate(ImagingStudy imagingStudy, Attributes attributes)
	{
		String studyDate = attributes.getString(Tag.StudyDate);
		if (studyDate != null)
		{
			try
			{
				var date = dateFormatter.parse(studyDate);
				imagingStudy.setStarted(date);
			}
			catch (java.text.ParseException e)
			{
				log.error("error parsing study date {}", studyDate, e);
			}

		}
	}

	private void setStudyEndpoint(ImagingStudy imagingStudy, Attributes attributes)
	{
		var studyRetrieveUrls = attributes.getStrings(Tag.RetrieveURL);
		if (studyRetrieveUrls != null)
		{
			var endPoint = new Endpoint().setAddress(studyRetrieveUrls[0]);
			var endpointReference = imagingStudy.addEndpoint();
			endpointReference.setResource(endPoint);
		}
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
