#include "TB6612FNG.h"

class TB6612FNG::TBMotor : public Wheels::Motor
{
	const int pin1;
	const int pin2;
	const int pinPWM;
	const String name;
	
	int speed;
	int val1;
	int val2;

	int NextSpeed()
	{
		static const int Step = 32;
		static const int initSpeed = 128;
		
		if (speed == 255)
		{
			return 255;
		}

		if (speed < initSpeed)
		{
			return initSpeed;
		}

		if (speed + Step > 255)
		return 255;

		return speed + Step;
	}

	void Rotate(int p1, int p2)
	{
		if (p1 != val1 || p2 != val2)
		{
			if (speed != 0)
			{
				Stop();
				delay(10);
			}
		}

		speed = NextSpeed();
		val1 = p1;
		val2 = p2;
		analogWrite(pinPWM, speed);
		digitalWrite(pin1, p1);
		digitalWrite(pin2, p2);
	}

	void Report(String op)
	{
		Serial.print("Engine ");
		Serial.print(name);
		Serial.print(" going ");
		Serial.print(op);
		Serial.print(" -- ");
		Serial.print(val1);
		Serial.print(':');
		Serial.print(val2);
		Serial.print(" with speed ");
		Serial.println(speed);      
	}
	
public:
	TBMotor(String n, int p1, int p2, int pPWM)
	: pin1(p1)
	, pin2(p2)
	, pinPWM(pPWM)
	, name(n)
	{
		pinMode(pin1, OUTPUT);
		pinMode(pin2, OUTPUT);
		pinMode(pinPWM, OUTPUT);
		speed=0;
		val1 = LOW;
		val2 = LOW;
	}

	void GoClockwise()
	{
		Rotate(HIGH, LOW);
		Report("Clockwise");
	}
	void GoCounterclockwise()
	{
		Rotate(LOW, HIGH);
		Report("Counterclockwise");
	}
	void Stop()
	{
		digitalWrite(pin1, LOW);
		digitalWrite(pin2, LOW);
		speed = 0;
		val1 = LOW;
		val2 = LOW;
		Report("Stop");
	}
	void Brake()
	{
		analogWrite(pinPWM, 0);      
		speed = 0;
		val1 = LOW;
		val2 = LOW;
		Report("Brake");
	}
};

TB6612FNG::TB6612FNG()
{
}

void TB6612FNG::begin(int pPWMA, int pINA2, int pINA1, int pSTDBY, int pINB1, int pINB2, int pPWMB)
{
	mA = new TBMotor("A", pINA1, pINA2, pPWMA);
	mB = new TBMotor("B", pINB1, pINB2, pPWMB);
	pinSTDBY = pSTDBY;
	pinMode(pinSTDBY, OUTPUT);
	digitalWrite(pinSTDBY, LOW);
}

void TB6612FNG::doStandby()
{
	digitalWrite(pinSTDBY, LOW);
	Serial.println("doStandby");
}

void TB6612FNG::doEnable()
{
	digitalWrite(pinSTDBY, HIGH);    
	Serial.println("doEnable");
}
