/*
 * main_tirtos.h
 *
 *  Created on: 23 במרץ 2018
 *      Author: shlomi
 */

#ifndef MAIN_TIRTOS_H_
#define MAIN_TIRTOS_H_

#ifdef __cplusplus
extern "C" {
#endif

int mqttsn_task_create(void);

void mqttsn_call_entry_thread(void);

#ifdef __cplusplus
}
#endif

#endif /* MAIN_TIRTOS_H_ */
