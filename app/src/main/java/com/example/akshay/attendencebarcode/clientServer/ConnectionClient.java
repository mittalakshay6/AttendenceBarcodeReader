package com.example.akshay.attendencebarcode.clientServer;

import java.io.IOException;
import java.net.Socket;

public class ConnectionClient {
    private Socket socket;
    public boolean connectToServer(String address, int port){
        try {
            socket = new Socket(address, port);
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }
}
