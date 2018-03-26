/*
 * Copyright (c) 2015-2016, Texas Instruments Incorporated
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * *  Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *  Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * *  Neither the name of Texas Instruments Incorporated nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 *  ======== uartecho.c ========
 */
#include "mqttsn_thread_entry.h"
#include <stdint.h>
#include <stddef.h>

/* Driver Header files */
#include <ti/drivers/GPIO.h>
#include <ti/drivers/UART.h>

/* Example/Board Header files */
#include "Board.h"

#include "mqttsn-lib/MQTTSNClient.h"
#include "mqttsn-lib/Network.h"
#include "mqttsn-lib/Timer.h"
#include "mqttsn-lib/MQTTSNPacket.h"
#include <string.h>
#include "mqttsn-transport/transport.h"

#include <ti/sysbios/knl/Task.h>
#include <ti/sysbios/knl/Semaphore.h>



/*
 *  ======== mainThread ========
 */
void mqttsn_thread_entry(UArg a0, UArg a1)
{
    /*
    UART_Handle uart;
    UART_Params uartParams;

    // Call driver init functions
    // GPIO_init();
    UART_init();

    // Create a UART with data processing off.
    UART_Params_init(&uartParams);
    //uartParams.readMode = UART_MODE_CALLBACK;
    uartParams.writeDataMode = UART_DATA_BINARY;
    uartParams.readDataMode = UART_DATA_BINARY;
    uartParams.readReturnMode = UART_RETURN_FULL;
    uartParams.readEcho = UART_ECHO_OFF;
    uartParams.baudRate = 115200;

    uart = UART_open(Board_UART0, &uartParams);

    if (uart == NULL) {
        // UART_open() failed
        while (1);
    }

    transport_open(uart);
    */

    /* Turn on user LED */
    GPIO_write(Board_GPIO_LED0, Board_GPIO_LED_ON);

    Network net;
    MQTTSN::Client<Network, Timer, 20, 5> client(net);

    MQTTSN_topicid topic;
    unsigned char* payload = (unsigned char*)"mypayload";
    int payloadlen = strlen((char*)payload);
    int retained = 0;

    MQTTSNPacket_connectData options = MQTTSNPacket_connectData_initializer;
    options.clientID.cstring = "shl1234567";
    options.duration = 0;

    topic.type = MQTTSN_TOPIC_TYPE_SHORT;
    memcpy(topic.data.short_name, "sh", 2);

    client.connect(options);

    while(1){
        client.publish(topic, payload, payloadlen, MQTTSN::QOS0, retained);
        Task_sleep(1000);
    }
    //client.yield();

}
