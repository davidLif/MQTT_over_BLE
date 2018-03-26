package blemqtt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.android.bluetoothlegatt.R;
import blemqtt.uartonble.UartOverBLE;

public class ConnectScreen extends Activity {
    private final String TAG = "MQTT_over_BLE";
    private BluetoothAdapter _mBluetoothAdapter;
    private BluetoothLeScanner _scanner;
    private String deviceAddress;
    private boolean discoveredMqttService;

    private ScanCallback findServiceByNameCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (!discoveredMqttService) {
                String discoveredDeviceName = result.getDevice().getName();
                if (UartOverBLE.UART_OVER_BLE_SERVICE_NAME.equals(discoveredDeviceName)) {
                    deviceAddress = result.getDevice().getAddress();
                    discoveredMqttService = true;
                    Log.i(TAG, "Found the service. its address is " + deviceAddress.toString());
                }
            }
        }
        @Override
        public void onScanFailed(final int errorCode) {
            Log.e(TAG, "Scan failed - error code " + Integer.toString(errorCode));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_screen);

        discoveredMqttService = false;
        StartBleScanner();
    }

    private void StartBleScanner() {
        try {
            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            _mBluetoothAdapter = bluetoothManager.getAdapter();

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (_mBluetoothAdapter == null || !_mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

                //And try again
                _mBluetoothAdapter = bluetoothManager.getAdapter();
            }
            else {
                _scanner = _mBluetoothAdapter.getBluetoothLeScanner();
                _scanner.startScan(findServiceByNameCallback);
            }
        }
        catch (Exception exp) {
            Log.e(TAG, "Exception on start ble scanner", exp);
            finish();
        }
    }

    public void ConnectToMQTTOnBLEDevice(View v) {

        if (discoveredMqttService) {
            _scanner.stopScan(findServiceByNameCallback);

            //Start new activity, and connect to the device
            Intent myIntent = new Intent(ConnectScreen.this, MessagesLogScreen.class);
            Log.i(TAG, "Connecting to device");
            myIntent.putExtra("Device_Address", deviceAddress);
            myIntent.putExtra("testLog", "This is the first log"); //Optional parameters
            ConnectScreen.this.startActivity(myIntent);
        }
        else {
            Log.i(TAG, "Failed to find the appropriate device. MQTT over BLE service wont start.");

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Device not found");
            alertDialog.setMessage("Failed to find the appropriate device. MQTT over BLE service wont start.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }
}
