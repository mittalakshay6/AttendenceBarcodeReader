package com.example.akshay.attendencebarcode.Connections;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataExchangeHelper {

    private Socket socket;
    private boolean isDataSent;

    private static final String TAG = "DataTransfer";
    public  DataExchangeHelper(Socket socket) {
        this.socket=socket;
    }

    public void sendData(String data){
        this.isDataSent=false;
        new DataSender().execute(data);
    }

    public boolean isDataSent(){
        return this.isDataSent;
    }

    class DataSender extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            DataOutputStream dataOutputStream;
            try {
                Log.d(TAG, "Data Transfer Initiated");
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(strings[0]);
                Log.d(TAG, "Data Transfer Completed Successfully");
                return true;
            } catch (IOException e) {
                Log.d(TAG, "Data Transfer failed");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            isDataSent=aBoolean;
        }
    }
}