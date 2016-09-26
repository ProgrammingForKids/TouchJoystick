#include "TB6612FNG.h"
#include "Log.h"

void TB6612FNG::Motor::Report(String op)
{
  Log("Engine ")(_name)(" going ")(op)(" -- ")(_val1)(':')(_val2)(" with speed ")(_speed);
}

TB6612FNG::Motor::Motor(String n, int p1, int p2, int pPWM)
: _pin1(p1)
, _pin2(p2)
, _pinPWM(pPWM)
, _name(n)
{
  _speed = 0;
  _val1 = LOW;
  _val2 = LOW;
}

void TB6612FNG::Motor::begin()
{
  pinMode(_pin1, OUTPUT);
  pinMode(_pin2, OUTPUT);
  pinMode(_pinPWM, OUTPUT);
}
    
void TB6612FNG::Motor::Set(int targetSpeed)
{
  String opname;
  
  if (targetSpeed > 0)
  {
    _val1 = HIGH;
    _val2 = LOW;
    opname = "Clockwise";
  }
  else if (targetSpeed < 0)
  {
    _val1 = LOW;
    _val2 = HIGH;
    opname = "Counterclockwise";
  }
  else
  {
    _val1 = LOW;
    _val2 = LOW;
    opname = "Stop";
  }
  _speed = abs(targetSpeed);

  digitalWrite(_pin1, _val1);
  digitalWrite(_pin2, _val2);
  analogWrite(_pinPWM, _speed);
  Report(opname);
}


void TB6612FNG::Motor::Brake()
{
  analogWrite(_pinPWM, 0);
  digitalWrite(_pin1, LOW);
  digitalWrite(_pin2, LOW);
  _speed = 0;
  _val1 = LOW;
  _val2 = LOW;
  Report("Brake");
}

TB6612FNG::TB6612FNG(int pPWMA, int pINA2, int pINA1, int pSTDBY, int pINB1, int pINB2, int pPWMB)
: _mLeft("Left", pINA1, pINA2, pPWMA)
, _mRight("Right", pINB1, pINB2, pPWMB)
{
  _pinSTDBY = pSTDBY;
}

void TB6612FNG::begin()
{
  _mLeft.begin();
  _mRight.begin();
  pinMode(_pinSTDBY, OUTPUT);
  digitalWrite(_pinSTDBY, LOW);
  _bEnabled = false;
}

void TB6612FNG::doStandby()
{
  if ( _bEnabled )
  {
    digitalWrite(_pinSTDBY, LOW);
    _bEnabled = false;
  }
  Log("doStandby");
}

void TB6612FNG::doEnable()
{
  if ( ! _bEnabled )
  {
    digitalWrite(_pinSTDBY, HIGH);
    _bEnabled = true;
  }
  Log("doEnable");
}


void TB6612FNG::Brake()
{
  Log("TB6612FNG::Brake");
  _mLeft.Brake();
  _mRight.Brake();
  doStandby();
}

void TB6612FNG::Go(unsigned short speedStep, unsigned short MaxSpeedStep, short leftFactor, short rightFactor)
{
  if (speedStep == 0)
  {
    Log("TB6612FNG::Go received speedStep=0, stopping");
    doStandby();
    return;
  }

  static const int FIRST_GEAR=64;
  static const int MAX_SPEED=255;
  // all speed steps must fit in FIRST_GEAR ... MAX_SPEED range
  
}

