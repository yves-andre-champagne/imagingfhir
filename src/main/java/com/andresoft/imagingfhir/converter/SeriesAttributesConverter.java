package com.andresoft.imagingfhir.converter;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Keyword;
import org.dcm4che3.data.Tag;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.ImagingStudy;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesComponent;

import com.andresoft.dicomweb.SeriesAttributesDataset;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeriesAttributesConverter extends BaseDicomWebAttributesConverter
{

	public ImagingStudySeriesComponent convert(ImagingStudy imagingStudy,
			SeriesAttributesDataset seriesAttributeDataSet)
	{
		var seriesComponent = imagingStudy.addSeries();

		var attributes = seriesAttributeDataSet.getSeries();

		seriesComponent.setUid(attributes.getString(Tag.SeriesInstanceUID));

		// series number
		var seriesNumber = attributes.getString(Tag.SeriesNumber);
		seriesComponent.setNumber(Integer.valueOf(seriesNumber));

		// series modality
		String seriesModality = attributes.getString(Tag.Modality);
		if (seriesModality != null)
		{
			seriesComponent.setModality(new Coding().setCode(seriesModality));
		}

		// series description
		String seriesDescription = attributes.getString(Tag.SeriesDescription);
		if (seriesDescription != null)
		{
			seriesComponent.setDescription(seriesDescription);
		}

		// series number of instances
		seriesComponent.setNumberOfInstances(seriesAttributeDataSet.getInstances().size());

		// body part examined
		String bodyPartExamined = attributes.getString(Tag.BodyPartExamined);
		if (bodyPartExamined != null)
		{
			seriesComponent.setBodySite(new Coding().setCode(bodyPartExamined));
		}

		String laterality = attributes.getString(Tag.Laterality);
		if (laterality != null)
		{
			seriesComponent.setLaterality(new Coding().setCode(laterality));
		}

		setSeriesStartedDate(seriesComponent, attributes);

		setSeriesEndpoint(seriesComponent, attributes);

		addStudyProcedureCode(imagingStudy, attributes);

		return seriesComponent;

	}

	private void setSeriesStartedDate(ImagingStudySeriesComponent seriesComponent, Attributes attributes)
	{
		String seriesDate = attributes.getString(Tag.SeriesDate);
		if (seriesDate != null)
		{
			try
			{
				var date = dateFormatter.parse(seriesDate);
				seriesComponent.setStarted(date);
			}
			catch (java.text.ParseException e)
			{
				log.error("error parsing series date {}", seriesDate, e);
			}

		}
	}

	private void setSeriesEndpoint(ImagingStudySeriesComponent seriesComponent, Attributes attributes)
	{
		var seriesRetrieveUrls = attributes.getStrings(Tag.RetrieveURL);
		if (seriesRetrieveUrls != null)
		{
			var endPoint = new Endpoint().setAddress(seriesRetrieveUrls[0]);
			var endpointReference = seriesComponent.addEndpoint();
			endpointReference.setResource(endPoint);
		}
	}

	private void addStudyProcedureCode(ImagingStudy imagingStudy, Attributes attributes)
	{
		var procedureCodeSeq = attributes.getSequence(Tag.ProcedureCodeSequence);
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

}
