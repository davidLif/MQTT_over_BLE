/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.util.Log;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
class SampleGattAttributes {
    private static final HashMap<String, String> attributes = new HashMap<>();
    private static final String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    //cc1350 services
    private static final String DEVICE_INFO = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    private static final String GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb";
    private static final String CC1350_EXAMPLE_ATTRIBUTES =  "0000fff0-0000-1000-8000-00805f9b34fb";

    //cc1350 UART characters
    private static final String TX_VALUE_CHARACTERISTIC =  "0000fff1-0000-1000-8000-00805f9b34fb";
    private static final String TX_PACKET_SERIAL_AND_LEN_CHARACTERISTIC =  "0000fff2-0000-1000-8000-00805f9b34fb";
    public static final String TX_ACK_CHARACTERISTIC =  "0000fff3-0000-1000-8000-00805f9b34fb";
    public static final String RX_NOTIFICATION_CHARACTERISTIC =  "0000fff4-0000-1000-8000-00805f9b34fb";

    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    static {
        // Sample Services
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(CLIENT_CHARACTERISTIC_CONFIG, "Client characteristic config");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        //cc1350 example services
        attributes.put(DEVICE_INFO, "Device information");
        attributes.put(GENERIC_ACCESS, "Generic access");
        attributes.put(GENERIC_ATTRIBUTE, "Generic attributes");
        attributes.put(CC1350_EXAMPLE_ATTRIBUTES, "CC1350 example attributes");

        //cc1350 UART characters
        attributes.put(TX_VALUE_CHARACTERISTIC, "Tx value");
        attributes.put(TX_PACKET_SERIAL_AND_LEN_CHARACTERISTIC, "Tx packet serial num and data len");
        attributes.put(TX_ACK_CHARACTERISTIC, "Tx ack");
        attributes.put(RX_NOTIFICATION_CHARACTERISTIC, "Rx as notifications");
    }

     static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        Log.i(TAG, "Attr uuid: " + uuid + "  Name: " + name);
        return name == null ? defaultName : name;
    }
}
