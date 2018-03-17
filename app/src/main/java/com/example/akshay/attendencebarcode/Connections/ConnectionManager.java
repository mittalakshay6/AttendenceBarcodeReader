package com.example.akshay.attendencebarcode.Connections;

import android.os.AsyncTask;
import android.util.Log;

import com.example.akshay.attendencebarcode.R;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager {
    private final String TAG = "ConnectionManager";
    private Socket socket;
    boolean isConnected=false;
    private ServerSocket serverSocket;
    private final int PORT = 1234;

    public void connectToServer(String ipAddress){
        ConnectionInitiator connectionInitiator = new ConnectionInitiator();
        connectionInitiator.execute(ipAddress);
    }
    public boolean isConnected(){
        if(isConnected) {
            return socket.isConnected();
        }
        else{
            return false;
        }
    }

    public Socket getSocket() {
        return socket;
    }

    class ConnectionInitiator extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Log.d(TAG, "Connection Initiation started" + PORT);
                socket = new Socket(strings[0], 1234);
                Log.d(TAG, "Connection Established with Server");
                if(socket.isConnected()) {
                    return true;
                }
                else{
                    return false;
                }
            } catch (IOException e) {
                Log.d(TAG, "Failed to establish connection");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            isConnected=aBoolean;
        }
    }
}