package com.example.android.BLE_MQTT_broker.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.bluetoothlegatt.BluetoothLeService;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.UartOverBLE;
import com.example.android.bluetoothlegatt.UartOverBLEReadyNotification;
import com.example.android.mqttsn.gateway.Gateway;

import java.util.ArrayList;
import java.util.List;

public class MessagesLogScreen extends Activity implements IDataupdate, UartOverBLEReadyNotification {
    private final String TAG = "MQTT_over_BLE";

    private UartOverBLE _uartOverBLE;
    private Gateway mqttsn_gateway;

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

            _uartOverBLE = new UartOverBLE(mDeviceAddress, this);
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            _uartOverBLE.BindService(this, gattServiceIntent); //The connection itself takes some time

            mqttsn_gateway = new Gateway();
            mqttsn_gateway.start(this);

            if (_uartOverBLE.isConnected()) {

                _logMsgsdata.add(intent.getStringExtra("testLog"));
            }
            else {
                _logMsgsdata.add("Blast you");
            }
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

        /* if (log.equals("Connected to the device.")) {
            byte[] testbyte = new byte[2];
            testbyte[0] = 'a';
            testbyte[1] = 'b';
            try {
                _uartOverBLE.TX_WriteBlocking(testbyte, 2, 10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void HandleUartOverBLEReady() {
        onLogUpdate("UART over BLE is ready.");

        //Set next stage - register UDP send on rx and make thread that receives UDP and writes to tx
    }
}
