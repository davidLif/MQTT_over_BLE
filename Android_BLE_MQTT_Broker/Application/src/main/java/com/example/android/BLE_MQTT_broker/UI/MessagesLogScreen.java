package com.example.android.BLE_MQTT_broker.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.bluetoothlegatt.BluetoothLeService;
import com.example.android.bluetoothlegatt.IUdpReceivedHandler;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.RxListener;
import com.example.android.bluetoothlegatt.UDP_Client;
import com.example.android.bluetoothlegatt.UartOverBLE;
import com.example.android.bluetoothlegatt.UartOverBLEReadyNotification;
import com.example.android.mqttsn.gateway.Gateway;

import java.util.ArrayList;
import java.util.List;

public class MessagesLogScreen extends Activity implements IDataupdate, UartOverBLEReadyNotification {
    private final String TAG = "MQTT_over_BLE";

    private UartOverBLE _uartOverBLE;
    private UDP_Client mUdpClient;
    private Gateway mqttsn_gateway;
    private UartToUdpAdapter mUdpUartadapter;

    private List<String> _logMsgsdata = new ArrayList<>();
    private ArrayAdapter<String> _arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_log_screen);

        try {
            Intent intent = getIntent();
            String mDeviceAddress = intent.getStringExtra("Device_Address");

            UartOverBLE.AskPremissions(this, this);

            mUdpUartadapter = new UartToUdpAdapter();

            _uartOverBLE = new UartOverBLE(mDeviceAddress, this);
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            _uartOverBLE.BindService(this, gattServiceIntent); //The connection itself takes some time

            _uartOverBLE.Rx_RegisterOnReceive(mUdpUartadapter);

            mqttsn_gateway = new Gateway();
            mqttsn_gateway.start(this);

            mUdpClient = new UDP_Client(mUdpUartadapter, this);
        }
        catch (Exception exp) {
            Log.e(TAG, "Exception on UART over BLE connection", exp);
            _logMsgsdata.add("Exception on UART over BLE connection");
        }

        ListView logsListView = findViewById(R.id.logMesgsLst);
        _arrayAdapter = new ArrayAdapter<>(this,R.layout.single_message_log_layout, R.id.logTextView, _logMsgsdata);
        logsListView.setAdapter(_arrayAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _uartOverBLE.restartConnection(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUdpClient.StopUdpListening();
        _uartOverBLE.stopConnection(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "On destroy has been called");
        mqttsn_gateway.shutDown();
    }

    public void onLogUpdate(String log) {
        Log.i(TAG, "On LogUpdate has been called with the data: " + log);
        _logMsgsdata.add(log);
        if (_logMsgsdata.size() > 20) {
            _logMsgsdata.remove(0);
        }
        _arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void HandleUartOverBLEReady() {
        onLogUpdate("UART over BLE is ready.");

        //Set next stage - register UDP send on rx and make thread that receives UDP and writes to tx
        mUdpClient.StartListening();

        //For Test - start sending
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            byte[] dataBuffer = new byte[2];
            dataBuffer[0] = 'a';
            dataBuffer[1] = 'b';
            if (_uartOverBLE.isConnected()) {
                boolean hasSent = _uartOverBLE.TX_WriteBlocking(dataBuffer, 2, 10000);
                Log.i(TAG, "Received from the MQTT broker the message: " + new String(dataBuffer, 0, 2));
                if (hasSent) {
                    onLogUpdate(new String(dataBuffer, 0, 2));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occurred while sending the Upd incoming message on BLE", e);
            e.printStackTrace();
        }
    }

    private class UartToUdpAdapter implements IUdpReceivedHandler, RxListener {

        @Override
        public void OnRxDataReceived(byte[] rxData, int len) {
            try {
                if (mUdpClient != null) {
                    byte[] msg = new byte[len];
                    for (int i = 0; i < len; i++) {
                        msg[i] = rxData[i];
                    }
                    mUdpClient.SendMessage(rxData);
                    Log.i(TAG, "Received msg from the device. Msg: " + new String(rxData, 0, len));
                    onLogUpdate(new String(rxData, 0, len));
                }
            } catch (Exception e) {
                Log.e(TAG, "An exception occurred while sending the Incoming Uart message to UDP", e);
                e.printStackTrace();
            }

        }

        @Override
        public void OnUdpDataReceived(byte[] dataBuffer, int len) {
            try {
                if (_uartOverBLE.isConnected()) {
                    _uartOverBLE.TX_WriteBlocking(dataBuffer, len, 100000);
                    Log.i(TAG, "Received from the MQTT broker the message: " + new String(dataBuffer, 0, len));
                    onLogUpdate(new String(dataBuffer, 0, len));
                }
            } catch (Exception e) {
                Log.e(TAG, "An exception occurred while sending the Upd incoming message on BLE", e);
                e.printStackTrace();
            }
        }
    }
}
