package com.zekitez.openwebifclient.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zekitez.openwebifclient.data.ServerData;
import com.zekitez.openwebifclient.internet.TestResultListener;
import com.zekitez.openwebifclient.internet.TestRunnable;
import com.zekitez.openwebifclient.internet.RequestResultListener;
import com.zekitez.openwebifclient.internet.RequestRunnable;
import com.zekitez.openwebifclient.data.Request;
import com.zekitez.openwebifclient.internet.TrustMngr;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import androidx.appcompat.app.AppCompatActivity;
import openwebifclient.R;

public class MainActivity extends AppCompatActivity implements TestResultListener, RequestResultListener {

    final String TAG = "MainActivity";
    ServerData data = new ServerData();
    TrustMngr trustMngr;

    Button buttonTest, buttonRequest;
    CheckBox checkboxAuthenticate;
    EditText editTextReceiverPort, editTextReceiverIpAddress, editTextUser, editTextPassword, editTextInformation;
    RadioGroup radioGroupScheme;
    TextView textViewResult, textViewTitle;

    View.OnClickListener buttonTestListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Button 'Test' clicked !");
            textViewResult.setText("...");
            new Thread(new TestRunnable(data, MainActivity.this)).start();
        }
    };

    View.OnClickListener buttonRequestListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Button 'Request' clicked !");
            textViewResult.setText("...");
            data.information = editTextInformation.getText().toString().trim();
            data.setUserPassword(editTextUser.getText().toString().trim(), editTextPassword.getText().toString().trim());
            new Thread(new RequestRunnable(data, MainActivity.this)).start();
        }
    };

    EditText.OnEditorActionListener ipAddressOnEditListener = new EditText.OnEditorActionListener(){
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (data.setIsValidIP4Address(editTextReceiverIpAddress.getText().toString().trim())) {
                editTextReceiverIpAddress.setTextColor(Color.YELLOW);
                buttonTest.setTextColor(Color.YELLOW);
            } else {
                editTextReceiverIpAddress.setTextColor(Color.RED);
                buttonTest.setTextColor(Color.WHITE);
            }
            return false;
        }
    };

    EditText.OnEditorActionListener portOnEditListener = new EditText.OnEditorActionListener(){
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (data.setValidPort(editTextReceiverPort.getText().toString().trim())){
                editTextReceiverPort.setTextColor(Color.YELLOW);
                buttonTest.setTextColor(Color.YELLOW);
            } else {
                editTextReceiverPort.setTextColor(Color.RED);
                buttonTest.setTextColor(Color.WHITE);
            }
            return false;
        }
    };

    RadioGroup.OnCheckedChangeListener schemeOnCheckedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if ( checkedId == R.id.SchemeHttp){
                data.scheme = Request.Scheme.http;
                editTextReceiverPort.setText(data.portHttp);
            } else {
                data.scheme = Request.Scheme.https;
                editTextReceiverPort.setText(data.portHttps);
            }
        }
    };

    CompoundButton.OnCheckedChangeListener authenticateOnCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            data.authenticate = isChecked;
        }
    };

    //---------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonRequest = findViewById(R.id.buttonRequest);
        buttonRequest.setOnClickListener(buttonRequestListener);

        buttonTest = findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(buttonTestListener);

        checkboxAuthenticate = findViewById(R.id.checkBoxAuthenticate);
        checkboxAuthenticate.setOnCheckedChangeListener(authenticateOnCheckedListener);

        editTextReceiverIpAddress = findViewById(R.id.editTextReceiverIpAddress);
        editTextReceiverIpAddress.setOnEditorActionListener(ipAddressOnEditListener);

        editTextReceiverPort = findViewById(R.id.editTextReceiverPort);
        editTextReceiverPort.setOnEditorActionListener(portOnEditListener);

        editTextUser = findViewById(R.id.editTextUser);
        editTextPassword = findViewById(R.id.editTextPassword);

        radioGroupScheme = findViewById(R.id.RadioGroupScheme);
        radioGroupScheme.setOnCheckedChangeListener(schemeOnCheckedListener);

        textViewResult = findViewById(R.id.textViewResult);
        textViewTitle = findViewById(R.id.textViewTitle);
        editTextInformation = findViewById(R.id.editTextInformation);

        trustMngr = new TrustMngr(getApplicationContext());
        try {
            // Replace the default TrustManager with a local version.
            // ToDo try the default Trustmanager before the local version.
            SSLContext sslc = SSLContext.getInstance("TLS");
            sslc.init(null, new TrustManager[]{trustMngr.getTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the saveed data from the SharedPreferences
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        data.scheme = Request.Scheme.valueOf( preferences.getString(getString(R.string.PrefScheme),data.scheme.toString()));
        radioGroupScheme.check( (data.scheme == Request.Scheme.http ? R.id.SchemeHttp : R.id.SchemeHttps) );

        data.ipAddress = preferences.getString(getString(R.string.PrefIpAddress), data.ipAddress);
        editTextReceiverIpAddress.setText(data.ipAddress);
        data.portHttp = preferences.getString(getString(R.string.PrefPortHttp), data.portHttp);
        data.portHttps = preferences.getString(getString(R.string.PrefPortHttps), data.portHttps);
        editTextReceiverPort.setText( (data.scheme == Request.Scheme.http ? data.portHttp : data.portHttps) );
        data.information = preferences.getString(getString(R.string.PrefInformation), data.information);
        editTextInformation.setText(data.information);

        data.authenticate = preferences.getBoolean(getString(R.string.PrefAuthenticate), data.authenticate);
        checkboxAuthenticate.setChecked(data.authenticate);
        editTextUser.setText( preferences.getString(getString(R.string.PrefUser), editTextUser.getText().toString()) );
        editTextPassword.setText( preferences.getString(getString(R.string.PrefPassword), editTextPassword.getText().toString()) );
    }

    @Override
    protected void onPause() {
        super.onPause();

        data.information = editTextInformation.getText().toString().trim();

        // Save the data in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(getString(R.string.PrefScheme), data.scheme.toString());
        editor.putString(getString(R.string.PrefIpAddress), data.ipAddress);
        editor.putString(getString(R.string.PrefPortHttp), data.portHttp);
        editor.putString(getString(R.string.PrefPortHttps), data.portHttps);
        editor.putString(getString(R.string.PrefInformation), data.information);

        editor.putBoolean(getString(R.string.PrefAuthenticate), data.authenticate);
        editor.putString(getString(R.string.PrefUser), editTextUser.getText().toString());
        editor.putString(getString(R.string.PrefPassword), editTextPassword.getText().toString());
        editor.commit();
    }

    //---------------------------

    @Override
    public void testResultListener(boolean isReachable, String who) {
        runOnUiThread(() -> {
            textViewResult.setText("Test "+who+( isReachable ? " SUCCESS" : " FAILED"));
            // Set some colors matching the result.
            if (isReachable){
                editTextReceiverIpAddress.setTextColor(Color.GREEN);
                editTextReceiverPort.setTextColor(Color.GREEN);
                buttonTest.setTextColor(Color.GREEN);
            } else {
                editTextReceiverIpAddress.setTextColor(Color.RED);
                editTextReceiverPort.setTextColor(Color.RED);
            }
        });
    }

    @Override
    public void requestResultListener(int responseCode, String reply) {
        runOnUiThread(() -> {
            textViewResult.setText(reply);
            // Set some colors matching the result.
            if (responseCode ==  HttpURLConnection.HTTP_UNAUTHORIZED) {
                if (data.authenticate) {
                    checkboxAuthenticate.setTextColor(Color.WHITE);
                    editTextUser.setTextColor(Color.RED);
                    editTextPassword.setTextColor(Color.RED);
                } else {
                    checkboxAuthenticate.setTextColor(Color.RED);
                }

            } else if (responseCode ==  HttpURLConnection.HTTP_NOT_FOUND){
                editTextInformation.setTextColor(Color.RED);

            } else if (responseCode ==  HttpURLConnection.HTTP_OK) {
                editTextInformation.setTextColor(Color.WHITE);
                checkboxAuthenticate.setTextColor(Color.WHITE);
                editTextUser.setTextColor(Color.WHITE);
                editTextPassword.setTextColor(Color.WHITE);
            }
            editTextReceiverIpAddress.setTextColor(Color.WHITE);
            editTextReceiverPort.setTextColor(Color.WHITE);
            buttonTest.setTextColor(Color.WHITE);
        });
    }

    @Override
    public void urlUsedListener(String url) {
        runOnUiThread(() -> {
            textViewTitle.setText(url);
        });
    }
}