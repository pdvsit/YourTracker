package com.app.servicehandler;

import android.net.SSLCertificateSocketFactory;
import android.util.Log;

import com.app.utility.AppLog;
import com.app.utility.ServiceCode;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by thinksysuser on 8/17/2016.
 */
public class ServiceUtility {

    public static String getDataFromServer(String endPoint) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
                /* forming th java.net.URL object */
            URL url = new URL(endPoint);
            urlConnection = (HttpURLConnection) url.openConnection();

            if (urlConnection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) urlConnection;
                httpsConn.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
                httpsConn.setHostnameVerifier(new AllowAllHostnameVerifier());
            }

                 /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json");

                /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");

                /* for Get request */
            urlConnection.setRequestMethod("GET");
            int statusCode = urlConnection.getResponseCode();
            Log.d("statusCode: ", statusCode + "");
                /* 200 represents HTTP OK */
            if (statusCode == ServiceCode.OK) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

            /* Close Stream */
                if (inputStream != null) {
                    inputStream.close();
                }

            } else if (statusCode == ServiceCode.SERVICE_UNAVAILABLE) {
                result = urlConnection.getResponseMessage();
            } else if (statusCode == ServiceCode.INTERNAL_SERVER) {
                result = urlConnection.getResponseMessage();
            } else if (statusCode == ServiceCode.NOT_FOUND) { //,, ,,
                result = urlConnection.getResponseMessage();
            } else if (statusCode == ServiceCode.METHOD_NOT_ALLOWED) {
                result = urlConnection.getResponseMessage();
            } else if (statusCode == ServiceCode.UNAUTHORIZED) {
                result = urlConnection.getResponseMessage();
            } else if (statusCode == ServiceCode.BAD_REQUEST) {
                result = urlConnection.getResponseMessage();
            } else if (statusCode == ServiceCode.FORBIDDEN) {
                result = urlConnection.getResponseMessage();
            } else if (statusCode == ServiceCode.REQUEST_TIMEOUT) { //,,,
                result = urlConnection.getResponseMessage();
            }  else if (statusCode == ServiceCode.BAD_GATEWAY) {
                result = urlConnection.getResponseMessage();
            }  else if (statusCode == ServiceCode.NOT_IMPLEMENTED) {
                result = urlConnection.getResponseMessage();
            }  else if (statusCode == ServiceCode.MOVED_PERMANENTLY) {
                result = urlConnection.getResponseMessage();
            }  else if (statusCode == ServiceCode.NO_CONTENT) {
                result = urlConnection.getResponseMessage();
            }  else {
                AppLog.Log("ELSE", urlConnection.getResponseMessage());
            }
        } catch (Exception e) {
            Log.d("TAG", e.getLocalizedMessage());
            result = e.getLocalizedMessage();
        }

        return result; //"Failed to fetch data!";
    }


    public static String sendDataToServer(String endPoint, String inputData) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
                /* forming th java.net.URL object */
            URL url = new URL(endPoint);
            urlConnection = (HttpURLConnection) url.openConnection();

                 /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json");

                /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");

                /* for Get request */
            urlConnection.setRequestMethod("POST");
            int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
            if (statusCode == HttpURLConnection.HTTP_OK) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

            /* Close Stream */
                if (inputStream != null) {
                    inputStream.close();
                }
                Log.d("statusCode: ", statusCode + "");
            } else {
                Log.d("statusCode: ", statusCode + "");
            }
        } catch (Exception e) {
            Log.d("TAG", e.getLocalizedMessage());
        }
        return result; //"Failed to fetch data!";
    }
}
