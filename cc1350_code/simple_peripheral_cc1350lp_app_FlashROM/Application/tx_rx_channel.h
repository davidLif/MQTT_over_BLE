/*
 * tx_rx_channel.h
 *
 *  Created on: Mar 7, 2018
 *      Author: david
 */

#ifndef APPLICATION_TX_RX_CHANNEL_H_
#define APPLICATION_TX_RX_CHANNEL_H_

#ifdef __cplusplus
extern "C"
{
#endif

/*********************************************************************
 * INCLUDES
 */
#include "hal_types.h"
/*********************************************************************
*  EXTERNAL VARIABLES
*/

/*********************************************************************
 * CONSTANTS
 */

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * FUNCTIONS
 */

/*
 * Send data by notification
 */
extern uint8 Tx_BleUnsafeSend(uint8 len, void *value);

/*
 * Try receive data.
 *
 * pValue - a buffer to which we copy the received data, if the receive is successful
 *
 * output - data length. If no data received, return 0.
 */

extern uint8 Rx_tryReceive(void *pValue);

extern uint8 Rx_receiveBlocking(void *pValue, int timeout);


//Private
//extern void WaitOnRxReceive(uint32_t timeoutInClicks);


/*********************************************************************
*********************************************************************/

#ifdef __cplusplus
}
#endif

#endif /* APPLICATION_TX_RX_CHANNEL_H_ */
