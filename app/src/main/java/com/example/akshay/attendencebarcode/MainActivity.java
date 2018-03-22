package com.example.akshay.attendencebarcode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akshay.attendencebarcode.Connections.ConnectionManager;
import com.example.akshay.attendencebarcode.Connections.DataExchangeHelper;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //TODO add feature to turn flash light on in case of low light

    private TextView regNView;
    private EditText ipAddrText;
    private EditText confirmView;
    private Button connectButton;
    private Socket socket;
    private DataExchangeHelper dataExchangeHelper;
    private ProgressBar progressBar;
    private DataExchangeHelper.DataExchangeHelperListener listener;

    public static final Character SEND_SUCCESS = 'S';
    public static final Character SEND_FAIL_INVALID = 'I';
    public static final Character SEND_SUCCESS_PROXY = 'P';

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "MainActivityClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.pb_loading_indicator);
        progressBar.setVisibility(View.INVISIBLE);
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
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Log.d(TAG, "Connection Initiation started");
                socket = new Socket(strings[0], 1234);
                Log.d(TAG, "Connection Established with Server");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setText("Connected");
                    }
                });
                return true;
            } catch (IOException e) {
                Log.d(TAG, "Failed to establish connection");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectButton.setText("Connection failed");
                    }
                });
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    regNView.setVisibility(View.VISIBLE);
                    regNView.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    if(socket!=null && socket.isConnected()){
                        dataExchangeHelper = new DataExchangeHelper(socket, listener = new DataExchangeHelper.DataExchangeHelperListener() {
                            @Override
                            public void onStart() {
                                progressBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCompleted() {
                                progressBar.setVisibility(View.INVISIBLE);
                                final DataExchangeHelper dataReceiveHelper = new DataExchangeHelper(socket, new DataExchangeHelper.DataExchangeHelperListener() {
                                    @Override
                                    public void onStart() {
                                        progressBar.setVisibility(View.VISIBLE);
                                        Toast.makeText(MainActivity.this, "Marking your attendance", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCompleted() {
                                        Character receivedData = DataExchangeHelper.getReceivedData();
                                        if(receivedData==SEND_SUCCESS){
                                            Toast.makeText(MainActivity.this, "Attendance Marked", Toast.LENGTH_SHORT).show();
                                            confirmView.setText("Congratulations, your attendance has been marked");
                                            confirmView.setVisibility(View.VISIBLE);
                                        }
                                        else if(receivedData==SEND_SUCCESS_PROXY){
                                            Toast.makeText(MainActivity.this, "Trying for a proxy?", Toast.LENGTH_SHORT).show();
                                            confirmView.setText("Trying for a proxy? Attendance has been marked but this incident will be reported");
                                            confirmView.setVisibility(View.VISIBLE);
                                        }
                                        // TODO proxy will be reported on second try. Make it correct
                                        else if(receivedData==SEND_FAIL_INVALID){
                                            Toast.makeText(MainActivity.this, "Invalid registration number. Please try again", Toast.LENGTH_SHORT).show();
                                            confirmView.setText("Provided Registration does not exist in the database. Please try again with a valid registration number");
                                            confirmView.setVisibility(View.VISIBLE);
                                        }
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onError() {
                                        Toast.makeText(MainActivity.this, "Attendance failed to mark", Toast.LENGTH_SHORT).show();
                                        confirmView.setText("Connect and try again");
                                        confirmView.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                                dataReceiveHelper.receiveData();
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(MainActivity.this, "Data Transfer Failed, please to connect again", Toast.LENGTH_SHORT).show();
                                confirmView.setText("Connect and try again");
                                confirmView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                        dataExchangeHelper.sendData(barcode.displayValue);
                    }
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Log.d(TAG, "There is Some error");
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
