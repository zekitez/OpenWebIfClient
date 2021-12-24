package com.zekitez.openwebifclient.data;

import android.util.Base64;


public class ServerData {

    public String portHttp = "80",
            portHttps = "443",
            ipAddress = "192.168.35.119",
            information="/web/signal?",
            encodedUserPassword = "";

    public Request.Scheme scheme = Request.Scheme.http;
    public boolean authenticate = false;

    public boolean setValidPort(String port) {
        if (port.length() > 0) {
            int value = Integer.parseInt(port);
            if (value >= 0 && value <= 65535) {
                if (scheme == Request.Scheme.http) {
                    this.portHttp = port;
                } else {
                    this.portHttps = port;
                }
                return true;
            }
        }
        return false;
    }


    public boolean setIsValidIP4Address(String ipAddress) {
        if (ipAddress.length() > 0 &&
                ipAddress.matches("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")) {

            String[] groups = ipAddress.split("\\.");

            for (int i = 0; i <= 3; i++) {
                String segment = groups[i];
                if (segment == null || segment.length() <= 0) {
                    return false;
                }

                int value = 0;
                try {
                    value = Integer.parseInt(segment);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (value > 255) {
                    return false;
                }
            }
            this.ipAddress = ipAddress;
            return true;
        }
        return false;
    }


    public void setUserPassword(String user, String password) {
        try {
            if (user != null) {
                String userPassword = user + ":" + password;
                byte[] userPasswordInBytes = userPassword.getBytes("UTF-8");
                encodedUserPassword = Base64.encodeToString(userPasswordInBytes, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}