#include "Network.h"
#include "mqttsn-transport/transport.h"


Network::Network()
{
}


Network::~Network()
{
}


int Network::write(unsigned char* buf, int length, unsigned long timeout)
{
	return transport_sendPacketBuffer(buf, length);
}

int Network::read(unsigned char* buf, int length, unsigned long timeout)
{
	return transport_getdata(buf, length);
}
