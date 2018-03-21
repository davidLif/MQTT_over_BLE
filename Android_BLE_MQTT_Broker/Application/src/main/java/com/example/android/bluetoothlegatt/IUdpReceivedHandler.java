package com.example.android.bluetoothlegatt;

/**
 * Created by david on 3/21/2018.
 */

public interface IUdpReceivedHandler {
    void OnUdpDataReceived(byte[] dataBuffer, int len);
}
