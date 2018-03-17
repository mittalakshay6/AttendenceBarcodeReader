package com.example.akshay.attendencebarcode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.akshay.attendencebarcode.Connections.ConnectionManager;
import com.example.akshay.attendencebarcode.Connections.DataExchangeHelper;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //TODO add feature to turn flash light on in case of low light

    TextView regNView;
    EditText ipAddrText;
    EditText confirmView;
    Button connectButton;
    ConnectionManager connectionManager;
    Socket socket;
    DataExchangeHelper dataExchangeHelper;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "MainActivityClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regNView = findViewById(R.id.regNView);
        ipAddrText = findViewById(R.id.ipAddrText);
        confirmView = findViewById(R.id.confirmView);
        connectButton = findViewById(R.id.connectButton);

        findViewById(R.id.startCameraBtn).setOnClickListener(this);
        findViewById(R.id.connectButton).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startCameraBtn) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
        if(v.getId() == R.id.connectButton){
            Log.d(TAG, "Initiating Connection");
            new ConnectionInitiator().execute(ipAddrText.getText().toString());
        }
    }
    class ConnectionInitiator extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Log.d(TAG, "Connection Initiation started");
                socket = new Socket(strings[0], 1234);
                Log.d(TAG, "Connection Established with Server");
                if(socket.isConnected()) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectButton.setText("Connected");
                        }
                    });
                    return true;
                }
                else{
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectButton.setText("Connection failed");
                        }
                    });
                    return false;
                }
            } catch (IOException e) {
                Log.d(TAG, "Failed to establish connection");
                return false;
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
//                    statusMessage.setText(R.string.barcode_success);
                    regNView.setVisibility(View.VISIBLE);
                    regNView.setText(barcode.displayValue);
                    confirmView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);

                    if(socket.isConnected()){
                        DataOutputStream dataOutputStream;
                        try {
                            Log.d(TAG, "Data Transfer Initiated");
                            dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            dataOutputStream.writeUTF(regNView.getText().toString());
                            Log.d(TAG, "Data Transfer Completed Successfully");
                        } catch (IOException e) {
                            Log.e(TAG, "Data Transfer failed "+e.getMessage());
                        }
                    }
                } else {
//                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
//                statusMessage.setText(String.format(getString(R.string.barcode_error),
//                        CommonStatusCodes.getStatusCodeString(resultCode)));
                Log.d(TAG, "There is Some error");
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
