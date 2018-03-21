package com.example.android.bluetoothlegatt;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.annotation.SuppressLint;
import android.os.AsyncTask;

public class UDP_Client {

    @SuppressLint("StaticFieldLeak")
    public void SendMessage(byte[] message)
    {
        new AsyncTask<byte[], Void, Void>()
        {
            @Override
            protected Void doInBackground(byte[]... params)
            {
                byte[] message = params[0];
                DatagramSocket ds = null;

                try
                {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), R.integer.udpPort);
                    ds.setBroadcast(true);
                    ds.send(dp);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
}
