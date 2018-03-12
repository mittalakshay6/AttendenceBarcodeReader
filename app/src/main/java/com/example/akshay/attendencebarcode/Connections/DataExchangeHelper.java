package com.example.akshay.attendencebarcode.Connections;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataExchangeHelper {

    private Socket socket;
    private boolean isDataSent;
    private String receivedData;
    private boolean isDataReceived;

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

    public boolean isDataReceived(){
        return this.isDataReceived;
    }

    public void receiveData(){
        this.isDataReceived=false;
        new DataReceiver().execute();
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
                Log.e(TAG, "Data Transfer failed "+e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            isDataSent=aBoolean;
        }
    }
    class DataReceiver extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            DataInputStream dataInputStream;
            Log.d(TAG,"Data Receiving initiated");
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                receivedData = dataInputStream.readUTF();
                Log.d(TAG, "Data Received successfully");
                return true;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            isDataReceived=aBoolean;
        }
    }
}