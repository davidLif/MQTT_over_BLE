#include "Timer.h"



Timer::Timer(unsigned long val) : val(val)
{
}


Timer::~Timer()
{
}

void Timer::countdown_ms(unsigned long count_val)
{
	this->val += count_val;
}

void Timer::countdown(unsigned long count_val)
{
    this->val += 1000 * count_val;
}

bool Timer::expired() const
{
	return this->val == 0;
}

unsigned long Timer::left_ms() const
{
    return this->val;
}
