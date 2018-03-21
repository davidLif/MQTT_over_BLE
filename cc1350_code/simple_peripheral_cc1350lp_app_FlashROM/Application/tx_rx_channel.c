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

 // Semaphore globally used to post events to the application thread
 static Semaphore_Handle rxSem;

 // Clock instances for internal periodic events.
 static Clock_Struct rxBlockingPeriodicClock;

/*********************************************************************
 * LOCAL VARIABLES
 */

/*********************************************************************
 * LOCAL FUNCTIONS
 */

 void ExTimeUpdate_clockHandler(UArg arg);

 void ExTimeUpdate_clockHandler(UArg arg)
 {
   // Wake up waiting rx
   Semaphore_post((Semaphore_Object *)rxSem);
 }

/*********************************************************************
 * PUBLIC FUNCTIONS
 */

uint8 BlockingFunctionDataInit() {
    //RX constructs
    Util_constructClock(&rxBlockingPeriodicClock, ExTimeUpdate_clockHandler,
                        RETRY_RECEIVE_PERIODIC_EVENT, 0, false, NULL);
    rxSem = Semaphore_create(0, NULL, NULL);
    if (rxSem == NULL) {
        return 0;
    }

    return 1;
}

uint8 Tx_BleUnsafeSend(uint8 len, void *value) {
    if (len > TX_VALUE_LEN) {
        return;
    }
    else if (len <= TX_VALUE_LEN) {
        if (SimpleProfile_SetParameter(TX_CHARACTERISTIC, len, value) == SUCCESS) {
            return 1;
        }
        else {
            return 0;
        }
    }
}

uint8 Rx_tryReceive(void *pValue) {
    uint8 receivedDataLen = 0;
    uint8_t sentSerial;

    if (SimpleProfile_GetParameter(SIMPLEPROFILE_CHAR1, pValue) == SUCCESS) { //Try read the rx characteristic

        sentSerial = ((uint8 *)pValue)[RX_VALUE_LEN - 1];
        if ((sentSerial & SENT_PACKET_SERIAL_NUM_MASK) != prevPacketSerialNum) { //If the serial number is different from the last receive, then a packet has been sent

            SimpleProfile_SetParameter(SIMPLEPROFILE_CHAR3, sizeof(uint8_t), &sentSerial); //Send ack
            receivedDataLen = sentSerial & SENT_PACKET_LEN_MASK; //Calc received data length
            prevPacketSerialNum = sentSerial & SENT_PACKET_SERIAL_NUM_MASK;
        }
    }

    return receivedDataLen;
}

uint8 Rx_receiveBlocking(void *pValue, int timeout) {
    uint8 receivedDataLen = Rx_tryReceive(pValue);
    uint8 accamulatedTime = 0;

    if (receivedDataLen == 0) {
        Util_startClock(&rxBlockingPeriodicClock);

        while (receivedDataLen == 0) {
            Semaphore_pend(rxSem, 0);

            uint8 receivedDataLen = Rx_tryReceive(pValue);

            accamulatedTime += RETRY_RECEIVE_PERIODIC_EVENT;
            if (accamulatedTime > timeout) {
                break;
            }
        }

        Util_stopClock(&rxBlockingPeriodicClock);
    }

    return receivedDataLen;
}
