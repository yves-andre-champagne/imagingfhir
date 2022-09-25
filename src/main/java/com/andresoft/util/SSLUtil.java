package com.andresoft.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SSLUtil
{
	@Getter
	static TrustManager[] trustAllCerts;

	@Getter
	static HostnameVerifier hostnameVerifier;

	static Optional<SSLContext> sslContext = null;

	static SSLParameters sslParams;

	static
	{
		trustAllCerts = new TrustManager[] { new X509TrustManager()
		{
			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
			{
				// Not implemented
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
			{
				// Not implemented
			}
		} };

		hostnameVerifier = new HostnameVerifier()
		{
			@Override
			public boolean verify(String s, SSLSession sslSession)
			{
				return true;
			}
		};

	}

	public static SSLParameters getNoOpHostindentificationParam()
	{
		sslParams = new SSLParameters();
		sslParams.setEndpointIdentificationAlgorithm("");
		return sslParams;

	}

	public static Optional<SSLContext> getTrustAllCertsSSLContext()
	{
		try
		{
			if (sslContext == null)
			{

				Properties props = System.getProperties();
				props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

				sslContext = Optional.of(SSLContext.getInstance("TLS"));

				sslContext.get().init(null, SSLUtil.getTrustAllCerts(), new java.security.SecureRandom());
			}
		}
		catch (Exception e)
		{

			sslContext = Optional.empty();
			log.error("Error generating SSL context", e);

		}
		return sslContext;
	}

}
