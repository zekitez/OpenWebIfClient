package com.zekitez.openwebifclient.internet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.zekitez.openwebifclient.activities.DecisionActivity;
import com.zekitez.openwebifclient.data.CertificateDecision;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TrustMngr {

    public final String identForText = "SelfSignedCertificateText";
    final String TAG = "TrustMngr";
    Context context;

    private static CertificateDecision certDecision = new CertificateDecision();

    private TrustManager localTrustmanager = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            Log.d(TAG, "getAcceptedIssuers");
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.d(TAG, "checkServerTrusted " + authType + " len:" + chain.length);
            if (chain != null && chain.length > 0) {
                chain[0].checkValidity();       // [0] is the server.
                chain[0].checkValidity(new Date());
                Log.i(TAG, "checkServerTrusted " + chain[0].toString());
                // Log.d(TAG, "checkServerTrusted " + chain[0].getSubjectX500Principal().getName()); // vusolo4k
                askToTrustCertificate(chain[0].getSubjectX500Principal().getName());
                try {
                    synchronized (certDecision) {
                        certDecision.wait(16000);    // wait for the answer but not infinite.
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (certDecision.isTrusted) {
                    return;
                }
                // Not trusted so throw exception.
                throw new CertificateException("You refused to trust the Certificate or hesitated too long.");
            }
            throw new CertificateException("X509Certificate chain is null or length is 0");
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.d(TAG,"checkClientTrusted " + authType);
            throw new CertificateException("Clients are not accepted.");
        }

        private void askToTrustCertificate(String text){
            Log.d(TAG,"askToTrust " + text+"\n");
            text = text.replace(",","\n");
            text = text.replace("OU=","OrganizationalUnit: ");
            text = text.replace("CN=","CommonName: ");
            text = text.replace("O=","Organization: ");
            text = "\n" +text;
            Intent intent = new Intent(context, DecisionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(identForText, text);
            context.startActivity(intent);    // Popup the question to trust the certificate.
        }

    };

    public TrustMngr(Context context){
        this.context = context;

        IntentFilter filter = new IntentFilter( context.getPackageName() );
        Log.d(TAG, context.getPackageName());
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"BroadcastReceiver");
                if ( intent.hasExtra("decision")) {
                    synchronized (certDecision) {
                        certDecision.isTrusted = intent.getBooleanExtra("decision", false);
                        certDecision.notify();          // Awake the waiting TrustManager
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    public TrustManager getTrustManager(){
        return localTrustmanager;
    }
}
