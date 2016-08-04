package me.kaede.feya.network;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
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

    // custom certificate with empty verify
    // work, but this is unsafe
    // https url connection
    public void testCustomCer1() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        String html = null;
        InputStream mInputStream = null;
        ByteArrayOutputStream mByteArrayOutputStream = null;
        HttpsURLConnection connection = null;
        try {
            URL mUrl = new URL(url);
            // empty trust manager
            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    for (X509Certificate item : chain) {
                        Log.d(TAG, "certificate = " + item.toString());
                    }
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    for (X509Certificate item : chain) {
                        Log.d(TAG, "certificate = " + item.toString());
                    }
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            // ssl socket factory
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
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
}
