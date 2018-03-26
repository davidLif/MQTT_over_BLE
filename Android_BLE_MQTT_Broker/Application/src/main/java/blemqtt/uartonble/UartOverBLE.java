package blemqtt.uartonble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 3/9/2018.
 */

public class UartOverBLE {
    private final static String TAG = "MQTT_over_BLE";
    public final static String UART_OVER_BLE_SERVICE_NAME = "SimpleBLEPeripheral";
    private final static int TX_RX_GATT_SERVICE_INDEX = 3;

    private final static int TX_VALUE_CHARACTERISTIC_INDEX = 0;
    private final static int TX_PACKAGE_SERIAL_NUM_AND_LEN_CHARACTERISTIC_INDEX = 1;
    private final static int TX_ACK_CHARACTERISTIC_INDEX = 2;
    private final static int RX_NOTIFICATION_CHARACTERISTIC_INDEX = 3;

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private List<BluetoothGattCharacteristic> mGattCharacteristics;
    private UartOverBLEReadyNotification mDataLogger;

    private byte txPacketSerialnumber;
    private final Object receivedAckLock;
    private boolean acquiredTxAckValue;
    private byte[] receivedAckData;

    private List<RxListener> rxListeners;
    private final Object rxLock;
    private boolean acquiredRx;
    private byte[] rxData;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth connection to " + UART_OVER_BLE_SERVICE_NAME);
                mBluetoothLeService = null;
            }
            else {
                Log.i(TAG, "Basic BLE service connection made. Connecting to the GATT service.");
                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(mDeviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i(TAG, "Service disconnected");
        }
    };

    // Handles various events fired by the Service.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i(TAG, "GATT services discovered. Will try to extract the appropriate characteristics data.");
                try {
                    List<BluetoothGattService> supportedServices =  mBluetoothLeService.getSupportedGattServices();
                    mGattCharacteristics = supportedServices.get(TX_RX_GATT_SERVICE_INDEX).getCharacteristics();

                    //Enable notifications so we will be able to receive from Rx
                    final BluetoothGattCharacteristic rxCharacter = mGattCharacteristics.get(RX_NOTIFICATION_CHARACTERISTIC_INDEX);
                    if ((rxCharacter.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(rxCharacter, true);
                    }

                    if (mDataLogger != null) mDataLogger.HandleUartOverBLEReady();
                }
                catch (Exception exp) {
                    Log.e(TAG, "An exception occurred while extracting the appropriate characteristics data.", exp);
                }
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                handleReceivedData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    public UartOverBLE(String deviceAddress, UartOverBLEReadyNotification dataLogger) {
        mDeviceAddress = deviceAddress;
        mDataLogger = dataLogger;

        txPacketSerialnumber = 0;

        receivedAckLock = new Object();
        acquiredTxAckValue = false;
        receivedAckData = null;

        rxListeners = new ArrayList<>();
        rxLock = new Object();
        acquiredRx = false;
        rxData = null;
    }

    public static void AskPremissions(Context context, Activity activity) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 2);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 5);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.INTERNET}, 5);
        }
    }

    public void BindService(Context context, Intent service) {
        context.bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
        //Should call restart connection after this function
    }

    public boolean isConnected() {
        return mBluetoothLeService != null && mBluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED;
    }

    //This method should be called if the activity's "OnResume" is called
    public void restartConnection(Context context, boolean tryBleRestart) {
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null && tryBleRestart) {
            final boolean result = mBluetoothLeService.connect(UART_OVER_BLE_SERVICE_NAME);
            Log.i(TAG, "Connect request result=" + result);
        }
    }

    //This method should be called if the activity's "OnPause" is called
    public void stopConnection(Context context) {
        context.unregisterReceiver(mGattUpdateReceiver);
    }

    public boolean TX_WriteBlocking(byte[] toWriteVal, int toWriteBytesLen, int timeout) throws InterruptedException {
        boolean hasSent = Tx_WriteUnsafe(toWriteVal, toWriteBytesLen);
        if (!hasSent) {
            return false;
        }

        return TX_AckCheck(timeout);
    }

    public boolean Tx_WriteUnsafe(byte[] toWriteVal, int toWriteBytesLen){

        if (toWriteVal.length < toWriteBytesLen || toWriteVal == null || toWriteBytesLen > 19) {
            return false;
        }

        Log.i(TAG, "Tx writing has been called. Len: " + String.valueOf(toWriteBytesLen));

        //Write the value
        byte[] buff = new byte[20];
        for (int i = 0; i < toWriteBytesLen; i++) {
            buff[i] = toWriteVal[i];
        }
        //Calc packet serial number
        txPacketSerialnumber++;
        if (txPacketSerialnumber > 0xE0) txPacketSerialnumber = 0;//circulate serial number
        //Calc packet meta
        byte txMeta = (byte)(toWriteBytesLen | (byte)((int)txPacketSerialnumber << 0x5));
        buff[19] = txMeta;
        BluetoothGattCharacteristic txValCharacteristic = mGattCharacteristics.get(TX_VALUE_CHARACTERISTIC_INDEX);
        txValCharacteristic.setValue(buff);
        mBluetoothLeService.writeCharacteristic(txValCharacteristic);


        //byte[] txMetaArr = new byte[1];
        //txMetaArr[0] = txMeta;
        //Write transfer metadata
        //BluetoothGattCharacteristic txMetaCharacteristic = mGattCharacteristics.get(TX_PACKAGE_SERIAL_NUM_AND_LEN_CHARACTERISTIC_INDEX);
        //txMetaCharacteristic.setValue(txMetaArr);
        //mBluetoothLeService.writeCharacteristic(txMetaCharacteristic);

        return true;
    }

    public boolean TX_AckCheck(int readWaitTimeout) throws InterruptedException {

        BluetoothGattCharacteristic txAckCharacteristic = mGattCharacteristics.get(TX_ACK_CHARACTERISTIC_INDEX);
        mBluetoothLeService.readCharacteristic(txAckCharacteristic);
        byte[] txAckCharacteristicValue;
        synchronized (receivedAckLock) {
            if (!acquiredTxAckValue) {
                receivedAckLock.wait(readWaitTimeout);
            }

            //If we didn't acquired the value after the timeout there is a problem with the connection
            if (!acquiredTxAckValue) return false;
            else {
                acquiredTxAckValue = false; //Set false for the next try
                txAckCharacteristicValue = receivedAckData;
            }
        }
        byte txLastAckedPacket = (byte)((txAckCharacteristicValue[0] & 0x1F) >> 0x1F);
        boolean isAcked = txPacketSerialnumber == txLastAckedPacket;
        Log.i(TAG, "Tx ack is " + String.valueOf(isAcked) + " . Read AckCharacteristicValue " + txAckCharacteristicValue[0]);
        return isAcked;
    }

    public void Rx_RegisterOnReceive(RxListener listener) {
        rxListeners.add(listener);
    }

    public byte[] Rx_receiveBlocking(int timeout) throws InterruptedException {
        byte[] rxDataLocal = null;
        synchronized (rxLock) {
            if (!acquiredRx) {
                rxLock.wait(timeout);
            }

            //If we didn't acquired the value after the timeout there is a problem with the connection
            if (!acquiredRx) return null;
            else {
                acquiredRx = false; //Set false for the next try
                rxDataLocal = rxData;
            }
        }
        return rxDataLocal;
    }

    public void RX_ClearOnlisteners() {
        rxListeners.clear();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void handleReceivedData(String bleReceviedStr) {
        //Get the UUID
        int delimiterLocation = bleReceviedStr.indexOf("\n");
        String uuid = bleReceviedStr.substring(0, delimiterLocation);
        byte[] dataAsByteArr = hexStringToByteArray(bleReceviedStr.substring(delimiterLocation + 1));

        if (uuid.equals(SampleGattAttributes.TX_ACK_CHARACTERISTIC)) {
            synchronized (receivedAckLock) {
                Log.i(TAG, "Tx ack is got value");
                receivedAckData = dataAsByteArr;
                acquiredTxAckValue = true;
                receivedAckLock.notifyAll();
            }
        }
        else if (uuid.equals(SampleGattAttributes.RX_NOTIFICATION_CHARACTERISTIC)) {
            int dataLen = dataAsByteArr[19];

            Log.i(TAG, "RX notification received. Msg " + new String(dataAsByteArr, 0, dataLen));
            for (RxListener listener: rxListeners) {
                listener.OnRxDataReceived(dataAsByteArr, dataLen);
            }
            synchronized (rxLock) {
                rxData = dataAsByteArr;
                acquiredRx = true;
                rxLock.notifyAll();
            }
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
