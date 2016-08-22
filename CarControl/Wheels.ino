#include "Wheels.h"

Wheels::Wheels()
{
  _mA = NULL;
  _mB = NULL;
}

bool Wheels::Left()
{
  Serial.println("Wheels::Left");
  doEnable();
  bool bA = _mA->GoCounterclockwise();
  bool bB = _mB->GoClockwise();
  return bA && bB;
}

bool Wheels::Right()
{
  Serial.println("Wheels::Right");
  doEnable();
  bool bA = _mA->GoClockwise();
  bool bB = _mB->GoCounterclockwise();
  return bA && bB;
}

bool Wheels::Forward()
{
  Serial.println("Wheels::Forward");
  doEnable();
  bool bA = _mA->GoClockwise();
  bool bB = _mB->GoClockwise();
  return bA && bB;
}

bool Wheels::Back()
{
  Serial.println("Wheels::Back");
  doEnable();
  bool bA = _mA->GoCounterclockwise();
  bool bB = _mB->GoCounterclockwise();
  return bA && bB;
}

bool Wheels::Stop()
{
  Serial.println("Wheels::Stop");
  bool bA = _mA->Stop();
  bool bB = _mB->Stop();
  if (bA && bB)
  {
    doStandby();
  }
  return true;
}

bool Wheels::Brake()
{
  Serial.println("Wheels::Brake");
  bool bA = _mA->Brake();
  bool bB = _mB->Brake();
  doStandby();
  return true;
}
