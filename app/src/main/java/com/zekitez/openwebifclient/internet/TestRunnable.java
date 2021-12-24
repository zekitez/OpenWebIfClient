package com.zekitez.openwebifclient.internet;

import android.util.Log;

import com.zekitez.openwebifclient.data.Request;
import com.zekitez.openwebifclient.data.ServerData;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TestRunnable implements Runnable {
    final String TAG = "PingRunnable";
    ServerData data;
    private TestResultListener callback;

    public TestRunnable(ServerData data, TestResultListener callback) {
        this.data = data;
        this.callback = callback;
    }

    @Override
    public void run() {
        boolean reachable = false;
        int port = 0;
        try {
            Socket pingSocket = new Socket();
            if (data.scheme == Request.Scheme.http) {
                port = Integer.parseInt(data.portHttp);
            } else {
                port = Integer.parseInt(data.portHttps);
            }
            Log.d(TAG, data.ipAddress + ":" + port + " ...");

            try {
                pingSocket.connect(new InetSocketAddress(data.ipAddress, port), 1000);
                reachable = pingSocket.isConnected();
                Log.d(TAG, data.ipAddress + ":" + port + " " + reachable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pingSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        callback.testResultListener(reachable, data.ipAddress + ":" + port);
    }

}
