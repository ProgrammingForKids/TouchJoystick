#include "Outlook.h"
#include "Log.h"

Outlook::Outlook(String name, int echo, int trig, unsigned long range, int led)
: _name(name)
, _pinEcho(echo)
, _pinTrig(trig)
, _delay_for_range(range * 1000 * 1000 * 2 / 100 / 340)
, _pinLed(led)
{
  Log("Outlook ")(_name)(" DURATION set to ")(_delay_for_range)(" for range of ")(range)(" cm");
}

void Outlook::begin()
{
  pinMode(_pinTrig, OUTPUT);
  pinMode(_pinEcho, INPUT);
  if (_pinLed > 0)
  {
    pinMode(_pinLed, OUTPUT);
    digitalWrite(_pinLed, LOW);
  }
  digitalWrite(_pinTrig, LOW);
  delayMicroseconds(15);
}


bool Outlook::isInRange()
{
  // The sensor is triggered by a HIGH pulse of 10 or more microseconds.
  // Give a short LOW pulse beforehand to ensure a clean HIGH pulse:
  digitalWrite(_pinTrig, HIGH);
  delayMicroseconds(10);
  digitalWrite(_pinTrig, LOW);

  // Read the signal from the sensor: a HIGH pulse whose
  // duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  //pinMode(_pinEcho, INPUT);
  unsigned long duration = pulseIn(_pinEcho, HIGH, _delay_for_range * 10);

  Log("Outlook ")(_name)(" Duration =")(duration);

  bool retval = (duration > 0 && duration <= _delay_for_range );
  
  if (_pinLed > 0)
  {
    digitalWrite(_pinLed, retval ? HIGH : LOW);
  }
  return retval;
}

