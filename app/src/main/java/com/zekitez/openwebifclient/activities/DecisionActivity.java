package com.zekitez.openwebifclient.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DecisionActivity extends Activity implements DialogInterface.OnCancelListener {

    final String TAG = "DecisionActivity";
    final String identForText = "SelfSignedCertificateText";
    AlertDialog dialog;

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String text = intent.getStringExtra(identForText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Untrusted certificate !!");
        builder.setMessage("Trust the certificate from\n"+text);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                publishDecision(true);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                publishDecision(false);
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onPause() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        publishDecision(false);
    }

    void publishDecision(boolean isTrusted) {
        Log.d(TAG,"publishDecision " + getApplicationContext().getPackageName() + " isTrusted "+isTrusted);
        Intent intent = new Intent(getApplicationContext().getPackageName());
        intent.putExtra("decision",isTrusted);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        finish();
    }

}
