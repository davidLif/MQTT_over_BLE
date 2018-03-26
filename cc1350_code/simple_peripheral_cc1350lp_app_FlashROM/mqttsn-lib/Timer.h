#pragma once
class Timer
{
public:
	Timer(unsigned long val = 0);
	virtual ~Timer();

	void countdown_ms(unsigned long count_val);
	void countdown(unsigned long count_val);
	bool expired() const;
	unsigned long left_ms() const;

private:
	unsigned long val;
};

