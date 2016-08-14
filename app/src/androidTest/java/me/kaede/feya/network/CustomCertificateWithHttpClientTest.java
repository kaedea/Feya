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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
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

    /**
     * https server with custom certificate
     * using {@link DefaultHttpClient}
     * should succeed, since we now have set custom certificate with custom KeyStore
     */
    public void testCustomCertificate1() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        String html = doHttpsWithCustomKeyStore(url);
        assertNotNull(html);
        Log.i(TAG, "html = " + html);
    }

    /**
     * https server with custom certificate
     * using {@link DefaultHttpClient}
     * should fail, since our custom KeyStore only have our custom certificate
     */
    public void testCustomCertificate2() {
        String url = "https://qq.hao.com";
        String html = doHttpsWithCustomKeyStore(url);
        assertTrue(TextUtils.isEmpty(html));
    }

    public String doHttpsWithCustomKeyStore(String url) {
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

    public static class SSLSocketFactoryEx extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
                UnrecoverableKeyException {
            super(truststore);
            TrustManager tm = new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
