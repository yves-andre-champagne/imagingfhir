package com.andresoft.imagingfhir.exception;

public class UnprocessableRequestException extends Exception
{

	private static final long serialVersionUID = -7581667812200820082L;

	public UnprocessableRequestException()
	{
		super();
	}

	public UnprocessableRequestException(String message)
	{
		super(message);
	}

	public UnprocessableRequestException(Throwable cause)
	{
		super(cause);
	}

}
