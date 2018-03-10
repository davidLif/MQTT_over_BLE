package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 3/9/2018.
 */

public class UartOverBLE {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private final static String UART_OVER_BLE_SERVICE_NAME = "SimpleBLEPeripheral";
    private final static int TX_RX_GATT_SERVICE_INDEX = 4;

    private final static int TX_VALUE_CHARACTERISTIC_INDEX = 0;
    private final static int TX_PACKAGE_SERIAL_NUM_AND_LEN_CHARACTERISTIC_INDEX = 1;
    private final static int TX_ACK_CHARACTERISTIC_INDEX = 2;

    private BluetoothLeService mBluetoothLeService;
    private List<BluetoothGattCharacteristic> mGattCharacteristics;
    private byte txPacketSerialnumber;

    private Object receivedAckLock;
    private boolean acquiredTxAckValue;
    private byte[] receivedAckData;

    private List<RxListener> rxListeners;

    private Object rxLock;
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
                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(UART_OVER_BLE_SERVICE_NAME);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                handleReceivedData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    public  UartOverBLE() {
        txPacketSerialnumber = 0;

        receivedAckLock = new Object();
        acquiredTxAckValue = false;
        receivedAckData = null;

        rxListeners = new ArrayList<>();
        rxLock = new Object();
        acquiredRx = false;
        rxData = null;
    }

    public boolean ConnectToDevice(Context context, Intent service) {
        context.bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (mBluetoothLeService != null && mBluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED) {
            //Get characters
            mGattCharacteristics = mBluetoothLeService.getSupportedGattServices()
                    .get(TX_RX_GATT_SERVICE_INDEX).getCharacteristics();
            restartConnection(context);
            return true;
        }
        else {
            return  false;
        }
    }

    //This method should be called if the activity's "OnResume" is called
    public void restartConnection(Context context) {
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(UART_OVER_BLE_SERVICE_NAME);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    //This method should be called if the activity's "OnPause" is called
    public void stopConnection(Context context) {
        context.unregisterReceiver(mGattUpdateReceiver);
    }

    public boolean Tx_WriteUnsafe(byte[] toWriteVal, int toWriteBytesLen){

        if (toWriteVal.length < toWriteBytesLen || toWriteVal == null || toWriteBytesLen > 20) {
            return false;
        }

        //Write the value
        BluetoothGattCharacteristic txValCharacteristic = mGattCharacteristics.get(TX_VALUE_CHARACTERISTIC_INDEX);
        txValCharacteristic.setValue(toWriteVal);
        mBluetoothLeService.writeCharacteristic(txValCharacteristic);

        //Calc packet serial number
        txPacketSerialnumber++;
        if (txPacketSerialnumber > 0xE0) txPacketSerialnumber = 0;//circulate serial number
        //Calc packet meta
        byte txMeta = (byte)(toWriteBytesLen | (byte)((int)txPacketSerialnumber << 0x1F));
        byte[] txMetaArr = new byte[1];
        txMetaArr[0] = txMeta;
        //Write transfer metadata
        BluetoothGattCharacteristic txMetaCharacteristic = mGattCharacteristics.get(TX_PACKAGE_SERIAL_NUM_AND_LEN_CHARACTERISTIC_INDEX);
        txMetaCharacteristic.setValue(txMetaArr);
        mBluetoothLeService.writeCharacteristic(txMetaCharacteristic);

        return true;
    }

    public boolean TX_WriteBlocking(byte[] toWriteVal, int toWriteBytesLen, int timeout) throws InterruptedException {
        boolean hasSent = Tx_WriteUnsafe(toWriteVal, toWriteBytesLen);
        if (!hasSent) {
            return false;
        }

        return TX_AckCheck(timeout);
    }


    public boolean TX_AckCheck(int readWaitTimeout) throws InterruptedException {
        BluetoothGattCharacteristic txAckCharacteristic = mGattCharacteristics.get(TX_ACK_CHARACTERISTIC_INDEX);
        mBluetoothLeService.readCharacteristic(txAckCharacteristic);
        byte[] txAckCharacteristicValue = null;
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
        return txPacketSerialnumber == txLastAckedPacket;
    }

    public void Rx_RegisterOnReceive(RxListener listener) {
        rxListeners.add(listener);
    }

    public byte[] Rx_receiveBlocking(int timeout) throws InterruptedException {
        byte[] rxData = null;
        synchronized (rxLock) {
            if (!acquiredRx) {
                rxLock.wait(timeout);
            }

            //If we didn't acquired the value after the timeout there is a problem with the connection
            if (!acquiredRx) return null;
            else {
                acquiredRx = false; //Set false for the next try
                rxData = rxData;
            }
        }
        return rxData;
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
                receivedAckData = dataAsByteArr;
                acquiredTxAckValue = true;
                receivedAckLock.notifyAll();
            }
        }
        else if (uuid.equals(SampleGattAttributes.RX_NOTIFICATION_CHARACTERISTIC)) {
            for (RxListener listener: rxListeners) {
                listener.OnRxDataReceived(dataAsByteArr);
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
