package com.example.akshay.attendencebarcode.Connections;

import android.os.AsyncTask;
import android.util.Log;

import com.example.akshay.attendencebarcode.R;

import java.io.IOException;
import java.net.Socket;

public class ConnectionManager {
    private final String TAG = "ConnectionManager";
    private Socket socket;
    private String ipAddress;

    public void connectToServer(String ipAddress){
        Connector connector = new Connector();
        connector.execute(ipAddress);
    }
    public boolean isConnected(){
        return socket.isConnected();
    }

    public Socket getSocket() {
        return socket;
    }

    class Connector extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Log.d(TAG, "Connection Initiation started");
                socket = new Socket(strings[0], R.integer.PORT);
                Log.d(TAG, "Connection Established with Server");
                return true;
            } catch (IOException e) {
                Log.d(TAG, "Failed to establish connection");
                return false;
            }
        }
    }
}