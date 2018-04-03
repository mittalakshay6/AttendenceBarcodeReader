package com.pyenoma.android.markme.Connections;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataExchangeHelper {

    private Socket socket;
    private static Character receivedData;
    private DataExchangeHelperListener listener;

    private static final String TAG = "DataTransfer";
    public  DataExchangeHelper(Socket socket,DataExchangeHelperListener listener) {
        this.socket=socket;
        this.listener=listener;
    }

    public void sendData(String data){
        if(socket == null || !socket.isConnected()){
            listener.onError();
            return;
        }
        new DataSender().execute(data);
    }

    public void receiveData(){
        if(socket == null || !socket.isConnected()){
            listener.onError();
            return;
        }
        new DataReceiver().execute();
    }

    class DataSender extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onStart();
        }

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
            if(aBoolean) {
                listener.onCompleted();
            }
            else {
                listener.onError();
            }
        }
    }
    class DataReceiver extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onStart();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DataInputStream dataInputStream;
            Log.d(TAG,"Data Receiving initiated");
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                receivedData = dataInputStream.readChar();
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
            if(aBoolean){
                listener.onCompleted();
            }
            else {
                listener.onError();
            }
        }
    }

    public static Character getReceivedData() {
        return receivedData;
    }

    public interface DataExchangeHelperListener{
        void onStart();
        void onCompleted();
        void onError();
    }
}