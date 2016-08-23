#include "Outlook.h"

Outlook::Outlook(int echo, int trig, unsigned long range)
: _pinEcho(echo)
, _pinTrig(trig)
, _delay_for_range(range * 1000 * 1000 * 2 / 100 / 340)
{
}

void Outlook::begin()
{
  pinMode(_pinTrig, OUTPUT);
  pinMode(_pinEcho, INPUT);
}


bool Outlook::isInRange()
{
  long duration;

  // The sensor is triggered by a HIGH pulse of 10 or more microseconds.
  // Give a short LOW pulse beforehand to ensure a clean HIGH pulse:
  digitalWrite(_pinTrig, LOW);
  delayMicroseconds(5);
  digitalWrite(_pinTrig, HIGH);
  delayMicroseconds(10);
  digitalWrite(_pinTrig, LOW);

  // Read the signal from the sensor: a HIGH pulse whose
  // duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  pinMode(_pinEcho, INPUT);
  duration = pulseIn(_pinEcho, HIGH, _delay_for_range * 10);

  // Serial.println(duration);

  if (duration == 0)
    return false;
  if (duration > _delay_for_range )
    return false;
  return true;
}

