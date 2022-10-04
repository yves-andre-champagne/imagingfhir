package com.andresoft.imagingfhir.converter;

import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesComponent;
import org.hl7.fhir.r4.model.ImagingStudy.ImagingStudySeriesInstanceComponent;

public class IntancesAttributeConverter extends BaseDicomWebAttributesConverter
{

	public List<ImagingStudySeriesInstanceComponent> convert(ImagingStudySeriesComponent seriesComponent,
			List<Attributes> instancesAttributes)
	{
		instancesAttributes.sort((attr1, attr2) -> Integer.valueOf(attr1.getString(Tag.InstanceNumber))
				.compareTo(Integer.valueOf(attr2.getString(Tag.InstanceNumber))));

		for (Attributes instanceAttr : instancesAttributes)
		{
			var instanceComponent = seriesComponent.addInstance();

			var uid = instanceAttr.getString(Tag.SOPInstanceUID);
			instanceComponent.setUid(uid);

			String sopClass = instanceAttr.getString(Tag.SOPClassUID);
			instanceComponent.setSopClass(new Coding().setCode(sopClass));
			String instanceNumber = instanceAttr.getString(Tag.InstanceNumber);

			instanceComponent.setNumber(Integer.parseInt(instanceNumber));
		}
		return seriesComponent.getInstance();
	}
}
