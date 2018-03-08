/*
 * tx_rx_channel.c
 *
 *  Created on: Mar 7, 2018
 *      Author: david
 */

/*********************************************************************
 * INCLUDES
 */
#include <string.h>

#include "gatt.h"
#include "linkdb.h"
#include "gapgattserver.h"
#include "gattservapp.h"
#include "devinfoservice.h"
#include "simple_gatt_profile.h"
#include "simple_peripheral.h"

#include "util.h"

#include "tx_rx_channel.h"


/* DONE */
/*********************************************************************
 * CONSTANTS
 */

#define TX_CHARACTERISTIC SIMPLEPROFILE_CHAR4

#define SENT_PACKET_SERIAL_NUM_MASK 224 //0xE0
#define SENT_PACKET_LEN_MASK 31 //0x1F

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
 * GLOBAL VARIABLES
 */

 static uint8 prevPacketSerialNum = 0;

/*********************************************************************
 * LOCAL VARIABLES
 */

/*********************************************************************
 * LOCAL FUNCTIONS
 */

/*********************************************************************
 * PUBLIC FUNCTIONS
 */

void Tx_BleUnsafeSend(uint8 len, void *value) {
    if (len > TX_VALUE_LEN) {
        return;
    }
    else if (len <= TX_VALUE_LEN) {
        SimpleProfile_SetParameter(TX_CHARACTERISTIC, len, value);
    }
}

uint8 Rx_tryReceive(void *pValue) {
    uint8 receivedDataLen = 0;
    uint8_t sentSerial;

    if (SimpleProfile_GetParameter(SIMPLEPROFILE_CHAR2, &sentSerial) == SUCCESS) { //Read the serial number of the last sent packet

        if ((sentSerial & SENT_PACKET_SERIAL_NUM_MASK) != prevPacketSerialNum) { //If the serial number is different from the last receive, then a packet has been sent

            SimpleProfile_GetParameter(SIMPLEPROFILE_CHAR1, pValue); //get the sent value
            SimpleProfile_SetParameter(SIMPLEPROFILE_CHAR3, sizeof(uint8_t), &sentSerial); //Send ack
            receivedDataLen = sentSerial & SENT_PACKET_LEN_MASK; //Calc received data length
            prevPacketSerialNum = sentSerial & SENT_PACKET_SERIAL_NUM_MASK;
        }
    }

    return receivedDataLen;
}
