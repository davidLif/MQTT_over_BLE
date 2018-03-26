package blemqtt.uartonble;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.bluetoothlegatt.R;

public class UDP_Client {

    private final static String TAG = "MQTT_over_BLE";
    private IUdpReceivedHandler mReceivedHandler;
    private volatile boolean mUdpListen;
    private Context mContext;

    private DatagramSocket ds;

    public UDP_Client(IUdpReceivedHandler receivedHandler, Context context) {
        mReceivedHandler = receivedHandler;
        mContext = context;
        try {
            ds = new DatagramSocket(mContext.getResources().getInteger(R.integer.udpPortReceive));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void SendMessage(byte[] message)
    {
        new AsyncTask<byte[], Void, Void>()
        {
            @Override
            protected Void doInBackground(byte[]... params)
            {
                Log.i(TAG, "Start UDP sending");
                byte[] message = params[0];
                String messageAsText = new String(message, 0 , message.length);
                int udpSendPort = mContext.getResources().getInteger(R.integer.udpPort);

                try
                {
                    DatagramPacket dp;
                    dp = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), udpSendPort);
                    ds.send(dp);
                    Log.i(TAG, "Sent UDP packet to port " + String.valueOf(udpSendPort) + ". Msg: " + messageAsText + ".Len: " + message.length);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to send UDP packet. Msg: " + messageAsText, e);
                }
                return null;
            }

            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
    }

    public void StartListening() {
        mUdpListen = true;

        //Start this on a new thread because no network work should start on the main thread
        new Thread() {
            public void run() {
                int port = mContext.getResources().getInteger(R.integer.udpPortReceive);
                byte[] buffer = new byte[2048];

                try {
                    //DatagramSocket udpListener = new DatagramSocket(port, InetAddress.getLocalHost());
                    DatagramPacket packetHolder = new DatagramPacket(buffer, buffer.length);

                    while (mUdpListen) {
                        Log.i(TAG, "UDP packet listening");
                        ds.receive(packetHolder);
                        String packetDataAsText = new String(buffer, 0, packetHolder.getLength());
                        Log.i(TAG, "UDP packet received " + packetDataAsText);
                        if (mReceivedHandler != null) {
                            mReceivedHandler.OnUdpDataReceived(buffer, packetHolder.getLength());
                        }

                        //Reset for the next receive
                        packetHolder.setLength(buffer.length);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "An exception occurred while listening on UDP packets from port " + port, e);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void StopUdpListening() {
        mUdpListen = false;
    }
}
