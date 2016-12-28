/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.security;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * dealing with https of self-signed certificate
 * using {@link HttpsURLConnection}
 * Created by Kaede on 16/8/4.
 */
public class CustomCertificateWithUrlConnectionTest extends InstrumentationTestCase {

    public static final String TAG = "CustomCertificateTest";
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    /**
     * https server with custom certificate
     * using {@link HttpsURLConnection}
     * will fail, because the Central KeyStore (System) do not have our custom certificate
     */
    public void testHttpsWithUrlConnection() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        String html = null;
        InputStream mInputStream = null;
        ByteArrayOutputStream mByteArrayOutputStream = null;
        HttpsURLConnection connection = null;
        try {
            // use https url connection
            URL mUrl = new URL(url);
            connection = (HttpsURLConnection) mUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            mInputStream = connection.getInputStream();
            mByteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = mInputStream.read(buffer)) != -1) {
                mByteArrayOutputStream.write(buffer, 0, len);
            }
            html = new String(mByteArrayOutputStream.toByteArray(), "utf-8");
        } catch (Exception e) {
            // will throw "javax.net.ssl.SSLHandshakeException: Unacceptable certificate: EMAILADDRESS=help@cac.washington.edu,
            // CN=UW Services CA, OU=UW Services, O=University of Washington, ST=WA, C=US"
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
        } finally {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mByteArrayOutputStream != null) {
                try {
                    mByteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }

        }
        assertTrue(TextUtils.isEmpty(html));
    }

    /**
     * use https url connection
     * use built-in certificates with empty TrustManager (no verifying)
     * work, but this is unsafe, the client will not will check server's validity
     */
    public void testHttpsWithEmptyVerifying() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        String html = null;
        InputStream mInputStream = null;
        ByteArrayOutputStream mByteArrayOutputStream = null;
        HttpsURLConnection connection = null;
        try {
            // empty trust manager
            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());

            // empty host name verifier
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // Always return true，接受任意域名服务器
                    return true;
                }
            };

            // use https url connection
            URL mUrl = new URL(url);
            connection = (HttpsURLConnection) mUrl.openConnection();

            // set ssl socket factory & host name verifier
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier(hostnameVerifier);

            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            mInputStream = connection.getInputStream();
            mByteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = mInputStream.read(buffer)) != -1) {
                mByteArrayOutputStream.write(buffer, 0, len);
            }
            html = new String(mByteArrayOutputStream.toByteArray(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
        } finally {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mByteArrayOutputStream != null) {
                try {
                    mByteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }

        }
        assertNotNull(html);
        Log.d(TAG, "url content = " + html);
    }


    public void testCustomCertificate1() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        String html = doHttpsWithCustomCertificate(url);
        assertNotNull(html);
        Log.d(TAG, "url content = " + html);
    }

    public void testCustomCertificate2() {
        String url = "https://hao.qq.com/";
        // will throw "java.net.ConnectException: Connection refused"
        // because our keystore only have our certificate now, and the default trust manager algorithm
        // will refuse the server's certificate since it does not match our custom certificate
        // there html stay null here
        String html = doHttpsWithCustomCertificate(url);
        assertTrue(TextUtils.isEmpty(html));
        Log.d(TAG, "url content = " + html);
    }

    /**
     * use https url connection
     * custom certificate with default TrustManager (default algorithm)
     */
    private String doHttpsWithCustomCertificate(String url) {
        assertTrue(!TextUtils.isEmpty(url));
        String html = null;
        InputStream mInputStream = null;
        ByteArrayOutputStream mByteArrayOutputStream = null;
        HttpsURLConnection connection = null;
        try {
            URL mUrl = new URL(url);
            connection = (HttpsURLConnection) mUrl.openConnection();
            // custom keystore
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // uwca.crt is custom certificate put in asset folder
            // provided by https://itconnect.uw.edu/security/securing-computer/install/safari-os-x/
            InputStream caInput = new BufferedInputStream(mContext.getAssets().open("uwca.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            caInput.close();

            // 1. Create a KeyStore containing our trusted CAs
            // but this keystore do not contain Android Central KeyStore, therefore it can only
            // work with our custom server url
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 2. Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // 3. Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLSv1", "AndroidOpenSSL");
            sslContext.init(null, tmf.getTrustManagers(), null);

            // 4. set ssl socket factory for the connection
            // or use HttpsURLConnection#setDefaultSSLSocketFactory for all connection
            connection.setSSLSocketFactory(sslContext.getSocketFactory());

            // 5. option, set host name verifier for our server.
            // HostnameVerifier is used to deal with the situation if the URL's hostname matches
            // the server's identification hostname (in the certificate).
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    //示例
                    Log.i(TAG, "hostname verify = " + hostname);
                    if ("certs.cac.washington.edu".equals(hostname)) {
                        return true;
                    } else {
                        HostnameVerifier hv =
                                HttpsURLConnection.getDefaultHostnameVerifier();
                        return hv.verify(hostname, session);
                    }
                }
            };
            connection.setHostnameVerifier(hostnameVerifier);

            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            mInputStream = connection.getInputStream();
            mByteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = mInputStream.read(buffer)) != -1) {
                mByteArrayOutputStream.write(buffer, 0, len);
            }
            html = new String(mByteArrayOutputStream.toByteArray(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
            return null;
        } finally {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mByteArrayOutputStream != null) {
                try {
                    mByteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return html;
    }


    public void testCustomTrustManager1() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        String html = doHttpsWithCustomTrustManager(url);
        assertNotNull(html);
        Log.d(TAG, "url content = " + html);
    }

    public void testCustomTrustManager2() {
        String url = "https://hao.qq.com/";
        // will throw "SignatureException"
        // because the server's certificate does not match our custom certificate
        // see {@link Certificate#verify}
        String html = doHttpsWithCustomTrustManager(url);
        assertTrue(TextUtils.isEmpty(html));
        Log.d(TAG, "url content = " + html);
    }


    /**
     * use https url connection
     * default certificate with custom TrustManager (custom algorithm)
     */
    private String doHttpsWithCustomTrustManager(String url) {
        assertTrue(!TextUtils.isEmpty(url));
        String html = null;
        InputStream mInputStream = null;
        ByteArrayOutputStream mByteArrayOutputStream = null;
        HttpsURLConnection connection = null;
        try {
            URL mUrl = new URL(url);
            connection = (HttpsURLConnection) mUrl.openConnection();
            // custom keystore
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // uwca.crt is custom certificate put in asset folder
            // provided by https://itconnect.uw.edu/security/securing-computer/install/safari-os-x/
            InputStream caInput = new BufferedInputStream(mContext.getAssets().open("uwca.crt"));
            final Certificate ca = cf.generateCertificate(caInput);
            caInput.close();

            // 1. Create a KeyStore containing our trusted CAs
            // but this keystore do not contain Android Central KeyStore, therefore it can only
            // work with our custom server url
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 2. Create a custom TrustManager[]
            TrustManager[] trustManagers = {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain,
                                                       String authType)
                                throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain,
                                                       String authType)
                                throws CertificateException {
                            for (X509Certificate cert : chain) {

                                // Make sure that it hasn't expired.
                                cert.checkValidity();

                                // Verify the certificate's public key chain.
                                // Verifies that this certificate was signed using the private key
                                // that corresponds to the specified public key.
                                try {
                                    cert.verify(ca.getPublicKey());
                                } catch (NoSuchAlgorithmException | InvalidKeyException
                                        | SignatureException | NoSuchProviderException e) {
                                    e.printStackTrace();
                                    Log.w(TAG, "verify exception = " + e);
                                    throw new CertificateException("invalidate certificate", e);
                                }
                            }
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // 3. Create an SSLContext that uses our TrustManager[]
            SSLContext sslContext = SSLContext.getInstance("TLSv1", "AndroidOpenSSL");
            sslContext.init(null, trustManagers, null);

            // 4. set ssl socket factory for the connection
            // or use HttpsURLConnection#setDefaultSSLSocketFactory for all connection
            connection.setSSLSocketFactory(sslContext.getSocketFactory());

            // 5. option, set host name verifier for our server.
            // HostnameVerifier is used to deal with the situation if the URL's hostname matches
            // the server's identification hostname (in the certificate).
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    //示例
                    Log.i(TAG, "hostname verify = " + hostname);
                    if ("certs.cac.washington.edu".equals(hostname)) {
                        return true;
                    } else {
                        HostnameVerifier hv =
                                HttpsURLConnection.getDefaultHostnameVerifier();
                        return hv.verify(hostname, session);
                    }
                }
            };
            connection.setHostnameVerifier(hostnameVerifier);

            // 6. do https request
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            mInputStream = connection.getInputStream();
            mByteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = mInputStream.read(buffer)) != -1) {
                mByteArrayOutputStream.write(buffer, 0, len);
            }
            html = new String(mByteArrayOutputStream.toByteArray(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
            return null;
        } finally {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mByteArrayOutputStream != null) {
                try {
                    mByteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return html;
    }
}
