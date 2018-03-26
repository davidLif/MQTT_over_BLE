#ifndef TRANSPORT_H_
#define TRANSPORT_H_

#if defined(__cplusplus) /* If this is a C++ compiler, use C linkage */
extern "C" {
#endif

#include <ti/drivers/UART.h>

int transport_sendPacketBuffer(unsigned char* buf, int buflen);
int transport_getdata(unsigned char* buf, int count);
int transport_open(UART_Handle uart);
int transport_close(void);

#ifdef __cplusplus /* If this is a C++ compiler, use C linkage */
}
#endif


#endif /* TRANSPORT_H */
