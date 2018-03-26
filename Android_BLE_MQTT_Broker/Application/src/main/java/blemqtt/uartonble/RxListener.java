package blemqtt.uartonble;

/**
 * Created by david on 3/10/2018.
 */

public interface RxListener {
    void OnRxDataReceived(byte[] rxData, int len);
}
