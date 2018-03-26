#include "transport.h"
#include "Application/tx_rx_channel.h"
#include "simple_peripheral.h"

static UART_Handle uart;
static int counter = 0;

extern uint8 SendOnOtherTask(uint8 len, void *value, uint32_t timeout);

int transport_sendPacketBuffer(unsigned char* buf, int buflen)
{
    //return UART_write(uart, buf, buflen);
    uint8 x = SendOnOtherTask(buflen, buf, 6000);
    if(x == 1)
        return buflen;
    else
        return 0;
}


int transport_getdata(unsigned char* buf, int count)
{
    return Rx_receiveBlocking(buf, 5000);
    /*int x = -1;
    if(counter == 0)
    {
        x = UART_read(uart, buf, 3);
        counter ++;
    }
    return x;
*/}

int transport_open(UART_Handle uart2)
{
    uart = uart2;
    return 1;
}

int transport_close()
{
    return 1;
}
