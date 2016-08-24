/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.security;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * test get https response
 * use Android's {@link HttpsURLConnection} & Apache's {@link HttpClient}
 * Created by Kaede on 16/8/1.
 */
public class SimpleHttpsTest extends InstrumentationTestCase {

    public static final String TAG = "CustomCertificateTest";
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    /**
     * curl normal https url
     * use {@link HttpsURLConnection}
     */
    public void testHttpsGetByHttpsUrlConnection() {
        String url = "https://hao.qq.com";
        String html = null;
        InputStream mInputStream = null;
        ByteArrayOutputStream mByteArrayOutputStream = null;
        HttpsURLConnection connection = null;
        try {
            URL mUrl = new URL(url);

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

    /**
     * curl normal https url
     * use {@link HttpClient}
     */
    public void testHttpsGetByHttpClient() {
        String url = "https://hao.qq.com";
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
            e.printStackTrace();
            Log.w(TAG, "exception = " + e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        assertNotNull(html);
        Log.d(TAG, "html = " + html);
    }
}
