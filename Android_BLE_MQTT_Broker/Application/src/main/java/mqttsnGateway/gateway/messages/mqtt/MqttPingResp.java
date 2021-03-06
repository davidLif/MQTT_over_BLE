/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Ian Craggs - initial API and implementation and/or initial documentation
 *******************************************************************************/

package mqttsnGateway.gateway.messages.mqtt;


/**
 * This object represents a Mqtt PINGRESP message.
 * 
 *
 */
public class MqttPingResp extends MqttMessage{

	/**
	 * MqttPingResp constructor.Sets the appropriate message type. 
	 */
	public MqttPingResp() {
		msgType = MqttMessage.PINGRESP;
	}
	
	
	/**
	 * MqttPingResp constructor.Sets the appropriate message type. 
	 */
	public MqttPingResp(byte[] data) {
		msgType = MqttMessage.PINGRESP;
	}
	
	/**
	 * Method to convert this message to a byte array for transmission
	 * @return A byte array containing the PINGRESP message as it should appear on the wire
	 */
	public byte[] toBytes() {
		int length = 2;
		byte[] data = new byte[length];
		data [0] = (byte)((msgType << 4) & 0xF0);
		data [1] = (byte)0x00;//add Remaining length fields
		return data;
	}
}