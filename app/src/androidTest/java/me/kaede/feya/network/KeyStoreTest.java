package me.kaede.feya.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Created by Kaede on 16/8/12.
 */
public class KeyStoreTest extends InstrumentationTestCase {
    public static final String TAG = "CustomCertificateTest";
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void testGetLocalKeyStore() {
        try {
            //// TODO: 16/8/7 is there a way to access device‘s central keystore ？
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Enumeration<String> aliases = keyStore.aliases();
            Log.i(TAG, "================= Android Central KeyStore =================");
            while (aliases.hasMoreElements()) {
                String item = aliases.nextElement();
                // get nothing here
                Log.i(TAG, "keystore alias = " + item);
            }

            Log.i(TAG, "Custom KeyStore");
            Log.i(TAG, "================= Custom KeyStore =================");
            Certificate ca = loadCertificateFromFile();
            keyStore.setCertificateEntry("custom ca", ca);
            aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String item = aliases.nextElement();
                // get only custom certificate here
                Log.i(TAG, "keystore alias = " + item);
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private Certificate loadCertificateFromFile() throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // uwca.crt is custom certificate put in asset folder
        // provided by https://itconnect.uw.edu/security/securing-computer/install/safari-os-x/
        InputStream caInput = new BufferedInputStream(mContext.getAssets().open("uwca.crt"));
        Certificate ca = cf.generateCertificate(caInput);
        Log.i(TAG, "distinguished name = " + ((X509Certificate) ca).getSubjectDN());
        Log.i(TAG, "public key = " + ca.getPublicKey());
        Log.i(TAG, "certificate = " + ca.toString());
        caInput.close();
        return ca;
    }

    public void testLoadCertificateFromString() {
        try {
            Log.i(TAG, "================= certificate from file =================");
            Certificate caFile = loadCertificateFromFile();
            Log.i(TAG, "================= certificate from String =================");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(getStringInputStream());
            Certificate caString = cf.generateCertificate(caInput);
            Log.i(TAG, "distinguished name = " + ((X509Certificate) caString).getSubjectDN());
            Log.i(TAG, "public key = " + caString.getPublicKey());
            Log.i(TAG, "certificate = " + caString.toString());
            caInput.close();

            assertTrue(caFile.getPublicKey().toString().equals(caString.getPublicKey().toString()));
            assertTrue(caFile.toString().equals(caString.toString()));
        } catch (CertificateException | IOException e) {
            e.printStackTrace();
        }
    }

    public InputStream getStringInputStream() throws UnsupportedEncodingException {
        // uwca.crt's content
        String uwca_crt = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEBzCCA3CgAwIBAgIBADANBgkqhkiG9w0BAQQFADCBlDELMAkGA1UEBhMCVVMx\n" +
                "CzAJBgNVBAgTAldBMSEwHwYDVQQKExhVbml2ZXJzaXR5IG9mIFdhc2hpbmd0b24x\n" +
                "FDASBgNVBAsTC1VXIFNlcnZpY2VzMRcwFQYDVQQDEw5VVyBTZXJ2aWNlcyBDQTEm\n" +
                "MCQGCSqGSIb3DQEJARYXaGVscEBjYWMud2FzaGluZ3Rvbi5lZHUwHhcNMDMwMjI1\n" +
                "MTgyNTA5WhcNMzAwOTAzMTgyNTA5WjCBlDELMAkGA1UEBhMCVVMxCzAJBgNVBAgT\n" +
                "AldBMSEwHwYDVQQKExhVbml2ZXJzaXR5IG9mIFdhc2hpbmd0b24xFDASBgNVBAsT\n" +
                "C1VXIFNlcnZpY2VzMRcwFQYDVQQDEw5VVyBTZXJ2aWNlcyBDQTEmMCQGCSqGSIb3\n" +
                "DQEJARYXaGVscEBjYWMud2FzaGluZ3Rvbi5lZHUwgZ8wDQYJKoZIhvcNAQEBBQAD\n" +
                "gY0AMIGJAoGBALwCo6h4T44m+7ve+BrnEqflqBISFaZTXyJTjIVQ39ZWhE0B3Laf\n" +
                "bbZYju0imlQLG+MEVAtNDdiYICcBcKsapr2dxOi31Nv0moCkOj7iQueMVU4E1Tgh\n" +
                "YIR2I8hqixFCQIP/CMtSDail/POzFzzdVxI1pv2wRc5cL6zNwV25gbn3AgMBAAGj\n" +
                "ggFlMIIBYTAdBgNVHQ4EFgQUVdfBM8b6k/gnPcsgS/VajliXfXQwgcEGA1UdIwSB\n" +
                "uTCBtoAUVdfBM8b6k/gnPcsgS/VajliXfXShgZqkgZcwgZQxCzAJBgNVBAYTAlVT\n" +
                "MQswCQYDVQQIEwJXQTEhMB8GA1UEChMYVW5pdmVyc2l0eSBvZiBXYXNoaW5ndG9u\n" +
                "MRQwEgYDVQQLEwtVVyBTZXJ2aWNlczEXMBUGA1UEAxMOVVcgU2VydmljZXMgQ0Ex\n" +
                "JjAkBgkqhkiG9w0BCQEWF2hlbHBAY2FjLndhc2hpbmd0b24uZWR1ggEAMAwGA1Ud\n" +
                "EwQFMAMBAf8wKwYDVR0RBCQwIoYgaHR0cDovL2NlcnRzLmNhYy53YXNoaW5ndG9u\n" +
                "LmVkdS8wQQYDVR0fBDowODA2oDSgMoYwaHR0cDovL2NlcnRzLmNhYy53YXNoaW5n\n" +
                "dG9uLmVkdS9VV1NlcnZpY2VzQ0EuY3JsMA0GCSqGSIb3DQEBBAUAA4GBAIn0PNmI\n" +
                "JjT9bM5d++BtQ5UpccUBI9XVh1sCX/NdxPDZ0pPCw7HOOwILumpulT9hGZm9Rd+W\n" +
                "4GnNDAMV40wes8REptvOZObBBrjaaphDe1D/MwnrQythmoNKc33bFg9RotHrIfT4\n" +
                "EskaIXSx0PywbyfIR1wWxMpr8gbCjAEUHNF/\n" +
                "-----END CERTIFICATE-----\n";
        return new ByteArrayInputStream(uwca_crt.getBytes("UTF-8"));
    }
}
