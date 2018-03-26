/*
 * mqttsn_thread_entry.h
 *
 *  Created on: 26 במרץ 2018
 *      Author: shlomi
 */

#ifndef MQTTSN_THREAD_ENTRY_H_
#define MQTTSN_THREAD_ENTRY_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <ti/sysbios/knl/Task.h>

void mqttsn_thread_entry(UArg a0, UArg a1);

#ifdef __cplusplus
}
#endif

#endif /* MQTTSN_THREAD_ENTRY_H_ */
