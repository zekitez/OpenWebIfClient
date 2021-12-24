package com.zekitez.openwebifclient.internet;

import android.util.Log;

import com.zekitez.openwebifclient.data.Request;
import com.zekitez.openwebifclient.data.ServerData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public class RequestRunnable implements Runnable {
    final static String TAG = "RequestRunnable";
    static ServerData data;
    private RequestResultListener callback;

    private static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            Log.i(TAG, "HostnameVerifier ip: " + hostname+"  port:"+session.getPeerPort());
            try {
                if (session.getPeerCertificates().length > 0) {
                    if (hostname.equalsIgnoreCase(session.getPeerHost()) &&
                        data.portHttps.equalsIgnoreCase(""+session.getPeerPort())) {
                        return true;
                    }
                }
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            }
            return false;
        }
    };


    public RequestRunnable(ServerData data, RequestResultListener callback) {
        this.data = data;
        this.callback = callback;
    }

    @Override
    public void run() {
        if (data.scheme == Request.Scheme.http){
            httpRequest();
        } else {
            httpsRequest();
        }
    }

    private void httpsRequest(){
        String reply = "";
        HttpsURLConnection connection = null;
        int responseCode = 0;
        try {
            String urlString = "https://"+data.ipAddress+":"+data.portHttps+data.information;
            callback.urlUsedListener(urlString);
            URL url = new URL(urlString);
            Log.d(TAG,urlString);

            connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier(hostnameVerifier);
            connection.setDoInput(true);
            connection.setConnectTimeout(2000);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Connection", "keep-alive");
            if (data.authenticate) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Basic " + data.encodedUserPassword);
            } else {
                connection.setRequestMethod("PUT");
            }

            responseCode = connection.getResponseCode();
            Log.i(TAG, "Server responded with: " + responseCode + " " + connection.getResponseMessage());

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line = "";
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = reader.readLine()) != null) {
                    reply = reply + line +"\n";
                }
                connection.disconnect();
            } else {
                reply = "*** " + responseCode + " " + connection.getResponseMessage();
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            reply = e.toString();
            connection.disconnect();
        } catch (Exception e){
            e.printStackTrace();
            reply = e.toString();
            connection.disconnect();
        }
        callback.requestResultListener(responseCode, reply);
    }

    private void httpRequest(){
        String reply = "";
        HttpURLConnection connection = null;
        int responseCode = 0;
        try {
            String urlString = "http://"+data.ipAddress+":"+data.portHttp+data.information;
            callback.urlUsedListener(urlString);
            URL url = new URL(urlString);
            Log.d(TAG,urlString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(2000);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Connection", "keep-alive");
            if (data.authenticate) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Basic " + data.encodedUserPassword);
            } else {
                connection.setRequestMethod("PUT");
            }

            responseCode = connection.getResponseCode();
            Log.i(TAG, "Server responded with: " + responseCode + " " + connection.getResponseMessage());

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line = "";
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = reader.readLine()) != null) {
                    reply = reply + line +"\n";
                }
                connection.disconnect();
            } else {
                reply = "*** " + responseCode + " " + connection.getResponseMessage();
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            reply = e.toString();
            connection.disconnect();
        } catch (Exception e){
            e.printStackTrace();
            reply = e.toString();
            connection.disconnect();
        }
        callback.requestResultListener(responseCode, reply);
    }

}
