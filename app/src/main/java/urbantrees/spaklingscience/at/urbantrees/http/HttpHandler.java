package urbantrees.spaklingscience.at.urbantrees.http;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * TODO doc
 * Created by Laurenz Fiala on 19/09/2017.
 * Fetches the beacon mapping, parses and provides it to the other app components.
 */
public class HttpHandler extends AsyncTask<HttpHandlerParams, Void, HttpHandlerResult> {

    private static final String LOGGING_TAG = HttpHandler.class.getName();

    /**
     * The reference to the calling activity for UI tasks and response.
     */
    private Activity activity;

    /**
     * Key is device address, value is mapped URL to display.
     */
    public Map<String, String> deviceUrlMapping = new HashMap<>();

    public HttpHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected HttpHandlerResult doInBackground(HttpHandlerParams... params) {

        if (params == null || params.length != 1) {
            throw new RuntimeException("HttpHandler may only take one parameter of type HttpHandlerParams.");
        }

        return this.executeInternal(params[0]);

    }

    private HttpHandlerResult executeInternal(HttpHandlerParams params) {

        Log.i(LOGGING_TAG, "Starting HTTP request to " + params.getUrl() + "...");

        HttpsURLConnection connection = null;
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        StringBuilder responsePayload = null;

        try {

            // TODO only create trustmanager once
            SSLContext context = null;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = this.getClass().getResourceAsStream("/assets/DigiCertCA.crt");
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } finally {
                    caInput.close();
                }

                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
            } catch (Throwable t) {
                Log.e(LOGGING_TAG, t.getMessage(), t);
            }


            URL url = new URL(params.getUrl());
            connection = (HttpsURLConnection) url.openConnection();

            // Connection parameters
            connection.setSSLSocketFactory(context.getSocketFactory());
            connection.setRequestMethod(params.getMethod().toString());
            connection.setReadTimeout(params.getTimeout());
            //connection.setDoOutput(true);
            //connection.setDoInput(true);

            // Headers
            connection.setRequestProperty("Content-Type", params.getContentType());
            for (Map.Entry<String, String> header : params.getHeaders().entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            // Send payload (if applicable)
            if (params.getMethod().hasOutput()) {
                writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(params.getValue());
                writer.flush();
            }

            // Collect payload (if applicable)
            if (params.getMethod().hasInput()) {
                try {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    responsePayload = new StringBuilder();
                    String output;
                    while ((output = reader.readLine()) != null) {
                        responsePayload.append(output);
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }

            final int responseCode = connection.getResponseCode();
            Log.i(LOGGING_TAG, "Response code received: " + responseCode);

            return new HttpHandlerResult(responseCode, responsePayload != null ? responsePayload.toString() : null);

        } catch (IOException e) {
            Log.e(LOGGING_TAG, e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

}
