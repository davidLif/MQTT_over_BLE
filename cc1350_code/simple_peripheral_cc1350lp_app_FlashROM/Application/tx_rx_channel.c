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

#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>
#include <ti/sysbios/knl/Semaphore.h>

#include "tx_rx_channel.h"

extern void WaitOnRxReceive(uint32_t timeoutInClicks);


/* DONE */
/*********************************************************************
 * CONSTANTS
 */

#define TX_CHARACTERISTIC SIMPLEPROFILE_CHAR4

#define SENT_PACKET_SERIAL_NUM_MASK 224 //0xE0
#define SENT_PACKET_LEN_MASK 31 //0x1F

#define RETRY_RECEIVE_PERIODIC_EVENT 1000

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
 * GLOBAL VARIABLES
 */

 static uint8 prevPacketSerialNum = 0;

 //static Semaphore_Handle receiveSemaphore;
 //Semaphore_Params semParams1;
 //Semaphore_Struct structSem1; /* Memory allocated at build time */

/*********************************************************************
 * LOCAL VARIABLES
 */

/*********************************************************************
 * LOCAL FUNCTIONS
 */

/*********************************************************************
 * PUBLIC FUNCTIONS
 */

uint8 Tx_BleUnsafeSend(uint8 len, void *value) {
    if (len > 20/*TX_VALUE_LEN*/) {
        return 0;
    }
    else if (len <= 20/*TX_VALUE_LEN*/ && len > 0) {
        if (SimpleProfile_SetParameter(TX_CHARACTERISTIC, len, value) == SUCCESS) {
            return 1;
        }
        else {
            return 0;
        }
    }
    else {
        return 0;
    }
}

uint8 Rx_tryReceive(void *pValue) {
    uint8 receivedDataLen = 0;
    uint8_t sentSerial;
    uint8 prevPacketSerialNum_local = prevPacketSerialNum;

    if (SimpleProfile_GetParameter(SIMPLEPROFILE_CHAR1, pValue) == SUCCESS) { //Try read the rx characteristic


        sentSerial = ((uint8 *)pValue)[20/*RX_VALUE_LEN*/ - 1];
        if (sentSerial != prevPacketSerialNum_local) { //If the serial number is different from the last receive, then a packet has been sent

            SimpleProfile_SetParameter(SIMPLEPROFILE_CHAR3, sizeof(uint8_t), &sentSerial); //Send ack
            receivedDataLen = sentSerial & SENT_PACKET_LEN_MASK; //Calc received data length
            prevPacketSerialNum_local = sentSerial;
            prevPacketSerialNum = prevPacketSerialNum_local;
        }
    }

    return receivedDataLen;
}

uint8 Rx_receiveBlocking(void *pValue, int timeout) {

    uint32_t timeoutInClicks;
    if (timeout == 0) {
        timeoutInClicks = BIOS_WAIT_FOREVER;
    }
    else {
        timeoutInClicks = timeout * (timeout/Clock_tickPeriod);
    }

    WaitOnRxReceive(timeoutInClicks);
    uint8 receivedDataLen = Rx_tryReceive(pValue);

    return receivedDataLen;
}
