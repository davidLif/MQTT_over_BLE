package blemqtt.uartonble;

/**
 * Created by david on 3/21/2018.
 */

public interface IUdpReceivedHandler {
    void OnUdpDataReceived(byte[] dataBuffer, int len);
}
