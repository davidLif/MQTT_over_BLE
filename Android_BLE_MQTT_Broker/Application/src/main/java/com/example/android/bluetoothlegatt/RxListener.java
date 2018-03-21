package com.example.android.bluetoothlegatt;

/**
 * Created by david on 3/10/2018.
 */

public interface RxListener {
    void OnRxDataReceived(byte[] rxData, int len);
}
