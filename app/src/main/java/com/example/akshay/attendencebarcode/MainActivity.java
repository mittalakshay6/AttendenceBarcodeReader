package com.example.akshay.attendencebarcode;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView regNView;
    EditText ipAddrText;
    EditText confirmView;
    Button connectButton;
    boolean isConnected;

    Socket socket;

    private static final int PORT = 1234;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

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

        if(isConnected){

        }
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
            Connection connection = new Connection();
            connection.execute(ipAddrText.getText().toString());
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

                    if(isConnected){
                        new DataTransfer().execute();
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
    class DataTransfer extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            DataOutputStream dataOutputStream = null;
            try {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(regNView.getText().toString());
            } catch (IOException e) {
                Log.d(TAG, "Data Transfer failed");
            }
            return null;
        }
    }
    class Connection extends AsyncTask<String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Log.d(TAG, "Creating Socket");
                socket = new Socket(strings[0], PORT);
                Log.d(TAG, "Socket connection successful");
                isConnected=true;
                return true;
            } catch (IOException e) {
                Log.d(TAG, "Socket creation Failed");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                connectButton.setText("Connected");
            }
            else{
                connectButton.setText("Connection Failed");
            }
            super.onPostExecute(aBoolean);
        }
    }

}
