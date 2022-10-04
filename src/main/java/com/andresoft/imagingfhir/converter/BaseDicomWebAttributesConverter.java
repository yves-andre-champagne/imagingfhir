package com.andresoft.imagingfhir.converter;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class BaseDicomWebAttributesConverter
{

	protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
}
