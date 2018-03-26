/******************************************************************************
 Copyright (c) 2008, 2014 IBM Corp.

 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 and Eclipse Distribution License v1.0 which accompany this distribution.

 The Eclipse Public License is available at
 http://www.eclipse.org/legal/epl-v10.html
 and the Eclipse Distribution License is available at
 http://www.eclipse.org/org/documents/edl-v10.php.

 Contributors:
 Ian Craggs - initial API and implementation and/or initial documentation
 */

package mqttsnGateway.gateway.utils;


import android.util.Log;

public class GatewayLogger {

	public static final String TAG = "MQTT_over_BLE";
	
	public final static int INFO  = 1;
	public final static int WARN  = 2;
	public final static int ERROR = 3;
	
	private static int MIN_LOG_LEVEL = INFO;
	

	public static void info(String msg) {
		Log.i(TAG, msg);
	}
	
	public static void warn(String msg) {
		Log.w(TAG, msg);
	}

	public static void error(String msg) {
		Log.e(TAG, msg);
	}

	
	public static void log(int logLevel, String msg) {
		if(logLevel >= MIN_LOG_LEVEL) {
			switch (logLevel){
				case INFO:
					info(msg);
					break;
				case WARN:
					warn(msg);
					break;
				case ERROR:
					error(msg);
					break;
				default:
			}
		}
	}

	
	public static void setMinLogLevel(int minLogLevel) {
		MIN_LOG_LEVEL = minLogLevel;
	}
}