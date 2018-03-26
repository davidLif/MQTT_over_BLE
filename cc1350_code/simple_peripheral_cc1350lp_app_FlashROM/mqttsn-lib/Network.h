#pragma once
class Network
{
public:
	Network();
	virtual ~Network();

	int write(unsigned char* buf, int length, unsigned long timeout);
	int read(unsigned char* buf, int length, unsigned long timeout);
};

