package com.example.android.mqttsn.gateway.utils;

import android.content.Context;
import java.util.Hashtable;
import com.example.android.bluetoothlegatt.R;
import com.example.android.mqttsn.gateway.exceptions.MqttsException;

public class ConfigurationParser {
	
	public ConfigurationParser() {}

	public static void parseFile(Context context) throws MqttsException {
		//load from configuration file the data related to the gateway

		int logLevel;
		String level = context.getResources().getString(R.string.logLevel);
		if(level.equalsIgnoreCase("INFO"))
			logLevel = GatewayLogger.INFO;
		else if(level.equalsIgnoreCase("WARN"))
			logLevel = GatewayLogger.WARN;
		else if(level.equalsIgnoreCase("ERROR"))
			logLevel = GatewayLogger.ERROR;
		else
			logLevel = GatewayLogger.INFO;

		GatewayLogger.setMinLogLevel(logLevel);

		int predfTopicIdSize = context.getResources().getInteger(R.integer.predfTopicIdSize);
		if(predfTopicIdSize < 1)
			throw new MqttsException("Predefined topic id size should be greater than 1");
		GWParameters.setPredfTopicIdSize(predfTopicIdSize);

		//load the predefined topic ids
		int[] predefinedTopicskeys = context.getResources().getIntArray(R.array.predefinedTopicsKeys);
		String[] predefinedTopicsValues = context.getResources().getStringArray(R.array.predefinedTopicsValues);
		Hashtable<Integer, String> table = new Hashtable<>();
		for (int i = 0; i < predefinedTopicskeys.length; i++) {
			int topicId = predefinedTopicskeys[i];
			String topicValue = predefinedTopicsValues[i];
			if(topicId > 0 && topicId <= GWParameters.getPredfTopicIdSize() && !table.containsValue(topicValue)){
				table.put(topicId, topicValue);
			}
		}
		GWParameters.setPredefTopicIdTable(table);

		GWParameters.setGwId(context.getResources().getInteger(R.integer.gwId));
		GWParameters.setAdvPeriod(context.getResources().getInteger(R.integer.advPeriod));
		GWParameters.setKeepAlivePeriod(context.getResources().getInteger(R.integer.keepAlivePeriod));
		GWParameters.setMaxRetries(context.getResources().getInteger(R.integer.maxRetries));
		GWParameters.setWaitingTime(context.getResources().getInteger(R.integer.waitingTime));
		GWParameters.setMaxMqttsLength(context.getResources().getInteger(R.integer.maxMqttsLength));
		GWParameters.setMinMqttsLength(context.getResources().getInteger(R.integer.minMqttsLength));
		GWParameters.setUdpPort(context.getResources().getInteger(R.integer.udpPort));
		GWParameters.setBrokerURL(context.getResources().getString(R.string.brokerURL));

		int brokerTcpPort = context.getResources().getInteger(R.integer.brokerTcpPort);
		if(brokerTcpPort < 1024 || brokerTcpPort > 65535)
			throw new MqttsException("TCP port number out of range");
		GWParameters.setBrokerTcpPort(brokerTcpPort);

		long handlerTimeout = context.getResources().getInteger(R.integer.handlerTimeout);
		if(handlerTimeout <=0)
			throw new MqttsException("Handler timeout should be greater than 0");
		GWParameters.setHandlerTimeout(handlerTimeout);

		long forwarderTimeout = context.getResources().getInteger(R.integer.forwarderTimeout);
		if(forwarderTimeout <=0)
			throw new MqttsException("Forwarder timeout should be greater than 0");
		GWParameters.setForwarderTimeout(forwarderTimeout);

		long checkingPeriod = context.getResources().getInteger(R.integer.checkingPeriod);
		if(checkingPeriod <=0)
			throw new MqttsException("Checking period should be greater than 0");
		GWParameters.setCkeckingPeriod(checkingPeriod);

		GWParameters.setSerialPortURL(context.getResources().getString(R.string.serialPortURL));
		GWParameters.setClientIntString(context.getResources().getString(R.string.clientInterfaces));
		GWParameters.setProtocolName(context.getResources().getString(R.string.protocolName));
		GWParameters.setProtocolVersion(context.getResources().getInteger(R.integer.protocolVersion));
		GWParameters.setRetain(context.getResources().getBoolean(R.bool.retain));
		GWParameters.setWillQoS(context.getResources().getInteger(R.integer.willQoS));
		GWParameters.setWillFlag(context.getResources().getBoolean(R.bool.willFlag));
		GWParameters.setCleanSession(context.getResources().getBoolean(R.bool.cleanSession));
		GWParameters.setWillTopic(context.getResources().getString(R.string.willTopic));
		GWParameters.setWillMessage(context.getResources().getString(R.string.willMessage));
	}	
}