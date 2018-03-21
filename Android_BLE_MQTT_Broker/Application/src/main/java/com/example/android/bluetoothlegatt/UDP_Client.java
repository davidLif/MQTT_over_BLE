package com.example.android.bluetoothlegatt;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class UDP_Client {

    private final static String TAG = "MQTT_over_BLE";
    private IUdpReceivedHandler mReceivedHandler;
    private volatile boolean mUdpListen;
    private Context mContext;

    public UDP_Client(IUdpReceivedHandler receivedHandler, Context context) {
        mReceivedHandler = receivedHandler;
        mContext = context;
    }

    @SuppressLint("StaticFieldLeak")
    public void SendMessage(byte[] message)
    {
        new AsyncTask<byte[], Void, Void>()
        {
            @Override
            protected Void doInBackground(byte[]... params)
            {
                byte[] message = params[0];
                String messageAsText = new String(message, 0 , message.length);
                DatagramSocket ds = null;
                int udpSendPort = mContext.getResources().getInteger(R.integer.udpPort);

                try
                {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), udpSendPort);
                    ds.setBroadcast(true);
                    ds.send(dp);
                    Log.i(TAG, "Sent UDP packet to port " + String.valueOf(udpSendPort) + ". Msg: " + messageAsText);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to send UDP packet. Msg: " + messageAsText, e);
                }
                finally
                {
                    if (ds != null)
                    {
                        ds.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
            }
        }.execute(message);
    }

    public void StartListening() {
        mUdpListen = true;

        //Start this on a new thread because no network work should start on the main thread
        new Thread() {
            public void run() {
                int port = mContext.getResources().getInteger(R.integer.udpPortReceive);
                byte[] buffer = new byte[2048];

                try {
                    DatagramSocket udpListener = new DatagramSocket(port);
                    DatagramPacket packetHolder = new DatagramPacket(buffer, buffer.length);

                    while (mUdpListen) {
                        udpListener.receive(packetHolder);
                        String packetDataAsText = new String(buffer, 0, packetHolder.getLength());
                        Log.i("UDP packet received", packetDataAsText);
                        if (mReceivedHandler != null) {
                            mReceivedHandler.OnUdpDataReceived(buffer, packetHolder.getLength());
                        }

                        //Reset for the next receive
                        packetHolder.setLength(buffer.length);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "An exception occurred while listening on UDP packets from port " + port);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void StopUdpListening() {
        mUdpListen = false;
    }
}
