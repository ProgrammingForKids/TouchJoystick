#include "Wheels.h"

Wheels::Wheels()
{
  _mA = NULL;
  _mB = NULL;
}

bool Wheels::Left()
{
  Log("Wheels::Left");
  doEnable();
  bool bA = _mA->GoCounterclockwise();
  bool bB = _mB->GoClockwise();
  return bA && bB;
}

bool Wheels::Right()
{
  Log("Wheels::Right");
  doEnable();
  bool bA = _mA->GoClockwise();
  bool bB = _mB->GoCounterclockwise();
  return bA && bB;
}

bool Wheels::Forward()
{
  Log("Wheels::Forward");
  doEnable();
  bool bA = _mA->GoClockwise();
  bool bB = _mB->GoClockwise();
  return bA && bB;
}

bool Wheels::Back()
{
  Log("Wheels::Back");
  doEnable();
  bool bA = _mA->GoCounterclockwise();
  bool bB = _mB->GoCounterclockwise();
  return bA && bB;
}

bool Wheels::Stop()
{
  Log("Wheels::Stop");
  bool bA = _mA->Stop();
  bool bB = _mB->Stop();
  if (bA && bB)
  {
    doStandby();
  }
  return bA && bB;
}

bool Wheels::Brake()
{
  Log("Wheels::Brake");
  bool bA = _mA->Brake();
  bool bB = _mB->Brake();
  doStandby();
  return true;
}
