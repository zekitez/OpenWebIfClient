package com.zekitez.openwebifclient.internet;

public interface RequestResultListener {
    void requestResultListener(int responseCode, String reply);
    void urlUsedListener(String url);
}
