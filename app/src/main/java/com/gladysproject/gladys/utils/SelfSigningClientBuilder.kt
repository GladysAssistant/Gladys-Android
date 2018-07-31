package com.gladysproject.gladys.utils

import android.annotation.SuppressLint

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import okhttp3.OkHttpClient

object SelfSigningClientBuilder {


    // Create a trust manager that does not validate certificate chains
    // Install the all-trusting trust manager
    // Create an ssl socket factory with our all-trusting manager
    val unsafeOkHttpClient: OkHttpClient
        get() {
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>,
                                                    authType: String) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>,
                                                    authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        return arrayOfNulls(0)
                    }
                })
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                return OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                        .hostnameVerifier(HostnameVerifier { hostname, session -> true }).build()

            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
}