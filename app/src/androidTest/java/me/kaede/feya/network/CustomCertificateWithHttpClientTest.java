/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.network;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Kaede on 16/8/14.
 */
public class CustomCertificateWithHttpClientTest extends InstrumentationTestCase {
    public static final String TAG = "CustomCertificateTest";
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    /**
     * https server with custom certificate
     * using {@link DefaultHttpClient}
     * will fail, because the Central KeyStore (System) do not have our custom certificate
     */
    public void testHttpsGetByHttpClient() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        String html = null;
        HttpClient httpClient = new DefaultHttpClient();
        assertNotNull(httpClient);

        HttpGet httpget = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpget);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i(TAG, "statusCode = " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    html = EntityUtils.toString(entity);
                }
            }
        } catch (Exception e) {
            // should throw exception here
            // javax.net.ssl.SSLHandshakeException: java.security.cert.CertPathValidatorException:
            // Trust anchor for certification path not found.
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        assertTrue(TextUtils.isEmpty(html));
    }

    public void testCustomCertificate1() {
        String url = "https://certs.cac.washington.edu/CAtest/";

        // should succeed, since we now have set custom certificate with custom KeyStore
        String html = doHttpsWithCustomKeyStore(url);
        assertNotNull(html);
        Log.i(TAG, "html = " + html);
    }

    public void testCustomCertificate2() {
        String url = "https://qq.hao.com";

        // should fail, since our custom KeyStore only have our custom certificate
        String html = doHttpsWithCustomKeyStore(url);
        assertTrue(TextUtils.isEmpty(html));
    }

    /**
     * https server with custom certificate
     * using {@link DefaultHttpClient}
     * using custom KeyStore with our server's custom certificate (self-signed)
     */
    private String doHttpsWithCustomKeyStore(String url) {
        String html = null;
        HttpGet httpget = new HttpGet(url);
        HttpClient httpClient = null;
        try {
            // custom keystore
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // uwca.crt is custom certificate put in asset folder
            // provided by https://itconnect.uw.edu/security/securing-computer/install/safari-os-x/
            InputStream caInput = new BufferedInputStream(mContext.getAssets().open("uwca.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            Log.i(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
            Log.i(TAG, "key=" + ca.getPublicKey());
            caInput.close();

            // 1. Create a KeyStore containing our trusted CAs
            // but this keystore do not contain Android Central KeyStore, therefore it can only
            // work with our custom server url
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 2. Create a SSLSocketFactory with our KeyStore
            SSLSocketFactory sslSocketFactory = new SSLSocketFactory(keyStore);
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sslSocketFactory, 443));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            // 3. create HttpClient
            httpClient = new DefaultHttpClient(ccm, params);

            // 4. execute HttpClient
            HttpResponse response = httpClient.execute(httpget);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i(TAG, "statusCode = " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    html = EntityUtils.toString(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return html;
    }

    public void testCustomSocketFactory1() {
        String url = "https://certs.cac.washington.edu/CAtest/";

        // should succeed, you know why~
        String html = doHttpsWithCustomSocketFactory(url);
        assertNotNull(html);
        Log.i(TAG, "html = " + html);
    }

    public void testCustomSocketFactory2() {
        String url = "https://qq.hao.com";

        // should fail, since our custom TrustManager had not yet support Central KeyStore (System)
        String html = doHttpsWithCustomSocketFactory(url);
        assertTrue(TextUtils.isEmpty(html));
    }

    /**
     * https server with custom certificate
     * using {@link DefaultHttpClient}
     * use SSLSocketFactory with custom TrustManager that trust our server's custom certificate (self-signed)
     */
    public String doHttpsWithCustomSocketFactory(String url) {
        String html = null;
        HttpGet httpget = new HttpGet(url);
        HttpClient httpClient = null;
        try {
            // custom keystore
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // uwca.crt is custom certificate put in asset folder
            // provided by https://itconnect.uw.edu/security/securing-computer/install/safari-os-x/
            InputStream caInput = new BufferedInputStream(mContext.getAssets().open("uwca.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            Log.i(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
            Log.i(TAG, "key=" + ca.getPublicKey());
            caInput.close();

            // 1. Create a KeyStore containing our trusted CAs
            // but this keystore do not contain Android Central KeyStore, therefore it can only
            // work with our custom server url
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 2. Create custom SSLSocketFactory
            SSLSocketFactory sslSocketFactory = new SSLSocketFactoryEx(keyStore);
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sslSocketFactory, 443));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            // 3. create HttpClient
            httpClient = new DefaultHttpClient(ccm, params);

            // 4. execute HttpClient
            HttpResponse response = httpClient.execute(httpget);
            int statusCode = response.getStatusLine().getStatusCode();
            Log.i(TAG, "statusCode = " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    html = EntityUtils.toString(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return html;
    }

    public static class SSLSocketFactoryEx extends SSLSocketFactory {
        SSLContext sslContext;
        KeyStore keyStore;

        public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            this.keyStore = truststore;
            init();
        }

        private void init() throws KeyManagementException, NoSuchAlgorithmException {
            // SSLSocketFactory use builtin SSLContext (and TrustManager)
            // so we need create and custom SSLContext
            TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {
                    for (X509Certificate cert : chain) {

                        // Make sure that it hasn't expired.
                        cert.checkValidity();

                        // Verify the certificate's public key chain.
                        try {
                            Certificate certificate = keyStore.getCertificate("ca");
                            cert.verify(certificate.getPublicKey());
                        } catch (KeyStoreException | NoSuchAlgorithmException | InvalidKeyException
                                | SignatureException | NoSuchProviderException e) {
                            e.printStackTrace();
                            Log.w(TAG, "verify exception = " + e);
                            throw new CertificateException("invalidate certificate", e);
                        }
                    }
                }
            };
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            // using custom SSLContext instead of the builtin one
            SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
            // BEGIN android-added
            /*
             * Make sure we have started the handshake before verifying.
             * Otherwise when we go to the hostname verifier, it directly calls
             * SSLSocket#getSession() which swallows SSL handshake errors.
             */
            sslSocket.startHandshake();
            // END android-added
            getHostnameVerifier().verify(host, sslSocket);
            return sslSocket;
        }

        @Override
        public Socket createSocket() throws IOException {
            // using custom SSLContext instead of the builtin one
            javax.net.ssl.SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            return (SSLSocket) socketFactory.createSocket();
        }
    }
}
