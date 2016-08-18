#include "Wheels.h"

Wheels::Wheels()
{
	mA = NULL;
	mB = NULL;
}

void Wheels::Left()
{
	Serial.println("Wheels::Left");
	doEnable();
	mA->GoCounterclockwise();
	mB->GoClockwise();
}

void Wheels::Right()
{
	Serial.println("Wheels::Right");
	doEnable();
	mA->GoClockwise();
	mB->GoCounterclockwise();
}

void Wheels::Forward()
{
	Serial.println("Wheels::Forward");
	doEnable();
	mA->GoClockwise();
	mB->GoClockwise();    
}

void Wheels::Back()
{
	Serial.println("Wheels::Back");
	doEnable();
	mA->GoCounterclockwise();
	mB->GoCounterclockwise();
}

void Wheels::Stop()
{
	Serial.println("Wheels::Stop");
	mA->Stop();
	mB->Stop();
	doStandby();
}

void Wheels::Brake()
{
	Serial.println("Wheels::Brake");    
	mA->Stop();
	mB->Stop();
	doStandby();
}
