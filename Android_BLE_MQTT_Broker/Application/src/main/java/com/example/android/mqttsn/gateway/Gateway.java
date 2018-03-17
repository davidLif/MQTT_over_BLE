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

package com.example.android.mqttsn.gateway;

import android.content.Context;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.example.android.mqttsn.gateway.core.Dispatcher;
import com.example.android.mqttsn.gateway.core.GatewayMsgHandler;
import com.example.android.mqttsn.gateway.exceptions.MqttsException;
import com.example.android.mqttsn.gateway.messages.Message;
import com.example.android.mqttsn.gateway.messages.control.ControlMessage;
import com.example.android.mqttsn.gateway.timer.TimerService;
import com.example.android.mqttsn.gateway.utils.ConfigurationParser;
import com.example.android.mqttsn.gateway.utils.GWParameters;
import com.example.android.mqttsn.gateway.utils.GatewayAddress;
import com.example.android.mqttsn.gateway.utils.GatewayLogger;

/**
 * This is the entry point of the MQTT-SN Gateway.
 * 
 */
public class Gateway {
	private Dispatcher dispatcher;
	private ShutDownHook shutdHook;

	public void StartOnDifferentThread(Context context) {
		Log.i(GatewayLogger.TAG, "MQTT-SN Gateway starting. Loading gateway params");

		try {
			ConfigurationParser.parseFile(context);
		} catch (MqttsException e) {
			e.printStackTrace();
			GatewayLogger.error("Failed to load Gateway parameters. Gateway cannot start.");
			System.exit(1);
		}
		GatewayLogger.info("Gateway paremeters loaded.");

		//Start this on a new thread because no network work should start on the main thread
		new Thread() {
			public void run() {
				//instantiate the timer service
				TimerService.getInstance();

				//instantiate the dispatcher
				dispatcher = Dispatcher.getInstance();

				//initialize the dispatcher
				dispatcher.initialize();

				//create the address of the gateway itself(see com.example.android.mqttsn.gateway.utils.GatewayAdress)
				byte[] addr = new byte[1];
				addr[0] = (byte)GWParameters.getGwId();

				InetAddress ip = null;

				try {
					ip = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
					GatewayLogger.error("Failed to create the address of the Gateway.Gateway cannot start.");
					System.exit(1);
				}

				int port = GWParameters.getUdpPort();

				GatewayAddress gatewayAddress = new GatewayAddress(addr,ip,port);
				GWParameters.setGatewayAddress(gatewayAddress);


				//create a new GatewayMsgHandler (for the connection of the gateway itself)
				GatewayMsgHandler gatewayHandler = new GatewayMsgHandler(GWParameters.getGatewayAddress());

				//insert this handler to the Dispatcher's mapping table
				dispatcher.putHandler(GWParameters.getGatewayAddress(), gatewayHandler);

				//initialize the GatewayMsgHandler
				gatewayHandler.initialize();

				//connect to the broker
				gatewayHandler.connect();

				//add a "listener" for catching shutdown events (Ctrl+C,etc.)
				shutdHook = new ShutDownHook();
				Runtime.getRuntime().addShutdownHook(shutdHook);
			}
		}.start();
	}

	public void start(Context context){
        StartOnDifferentThread(context);
	}

	public void shutDown(){
		//generate a control message 
		ControlMessage controlMsg = new ControlMessage();
		controlMsg.setMsgType(ControlMessage.SHUT_DOWN);

		//construct an "internal" message and put it to dispatcher's queue
		//@see com.example.android.mqttsn.gateway.core.Message
		Message msg = new Message(null);
		msg.setType(Message.CONTROL_MSG);
		msg.setControlMessage(controlMsg);
		dispatcher.putMessage(msg);
	}

	private class ShutDownHook extends Thread{
		public void run(){
			shutDown();	
		}
	}
	

	public void removeShutDownHook() {
		Runtime.getRuntime().removeShutdownHook(shutdHook);		
	}	
}
