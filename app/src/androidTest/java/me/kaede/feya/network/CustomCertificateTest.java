package me.kaede.feya.network;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import android.util.Log;

import javax.net.ssl.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * dealing with https of self-signed certificate
 * Created by Kaede on 16/8/4.
 */
public class CustomCertificateTest extends InstrumentationTestCase {

    public static final String TAG = "CustomCertificateTest";
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testGetKeyStore() {
        try {
            //// TODO: 16/8/7 is there a way to access device central keystore
            Log.i(TAG, "Android Central KeyStore");
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String item = aliases.nextElement();
                Log.i(TAG, "keystore alias = " + item);
            }

            Log.i(TAG, "Custom KeyStore");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // uwca.crt 打包在 asset 中，该证书可以从https://itconnect.uw.edu/security/securing-computer/install/safari-os-x/下载
            InputStream caInput = new BufferedInputStream(mContext.getAssets().open("uwca.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            Log.i(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
            Log.i(TAG, "key=" + ca.getPublicKey());
            caInput.close();
            keyStore.setCertificateEntry("ca", ca);
            aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String item = aliases.nextElement();
                Log.i(TAG, "keystore alias = " + item);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            // ssl socket factory
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // empty host name verifier
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // Always return true，接受任意域名服务器
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

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
        String url = "https://www.google.com";
        String html = doHttpsWithCustomCertificate(url);
        assertNotNull(html);
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
            // uwca.crt 打包在 asset 中，该证书可以从https://itconnect.uw.edu/security/securing-computer/install/safari-os-x/下载
            InputStream caInput = new BufferedInputStream(mContext.getAssets().open("uwca.crt"));
            Certificate ca = cf.generateCertificate(caInput);
            Log.i("Longer", "ca=" + ((X509Certificate) ca).getSubjectDN());
            Log.i("Longer", "key=" + ca.getPublicKey());
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
        return html;
    }
}
