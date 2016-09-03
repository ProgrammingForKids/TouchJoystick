#include "Wheels.h"

Wheels::Wheels()
{
  _mA = NULL;
  _mB = NULL;
}


static int SpeedStep(int oldVal, int newVal)
{
  static int speedSlowest = 160;
  static int step = 32;

  if (newVal > oldVal)
  {
    if (newVal - oldVal < step)
      return newVal;
      
    oldVal += step;
    if (oldVal < speedSlowest)
      return speedSlowest;
    else
      return oldVal;
  }
  else// if (newVal <= oldVal)
  {
    if (oldVal - newVal < step)
      return newVal;

    oldVal -= step;
    
    if (oldVal < speedSlowest)
      return newVal;
    else
      return oldVal;
  }
}

Wheels::eCompletion Wheels::SetTarget(Wheels::Motor::eDir targetDirA, Wheels::Motor::eDir targetDirB, int targetSpeed)
{
  const Motor::eDir currentDirA = _mA->Direction();
  const Motor::eDir currentDirB = _mB->Direction();
  const int currentSpeedA = _mA->Speed();
  const int currentSpeedB = _mB->Speed();

 
  if (  // Proper direction already
        (targetDirA == currentDirA && targetDirB == currentDirB)
        ||
        // Already stopped
        (currentDirA == Motor::dirNone && currentDirB == Motor::dirNone) )
  {
    int newSpeedA = SpeedStep(currentSpeedA, targetSpeed);
    int newSpeedB = SpeedStep(currentSpeedB, targetSpeed);
    _mA->Set(targetDirA, newSpeedA);
    _mB->Set(targetDirB, newSpeedB);
    // return true if the target speed was reached at the last Go()
    if ( (targetSpeed == newSpeedA) && (targetSpeed == newSpeedB) )
      return DONE;
    else
      return STARTING;
  }

  // Otherwise decelerate and stop before going further
  int newSpeedA = SpeedStep(currentSpeedA, 0);
  int newSpeedB = SpeedStep(currentSpeedB, 0);
  _mA->Set(newSpeedA==0 ? Motor::dirNone : currentDirA, newSpeedA);
  _mB->Set(newSpeedB==0 ? Motor::dirNone : currentDirB, newSpeedB);

  if (newSpeedA == 0 && newSpeedB == 0)
  {
    doStandby();
    if (targetSpeed == 0)
      return DONE;
    else
      return STOPPED;
  }
  else
  {
    return STOPPING;
  }
}

Wheels::eCompletion Wheels::Left()
{
  Log("Wheels::Left");
  doEnable();
  return SetTarget(Motor::dirCounterclockwise, Motor::dirClockwise, 255);
}

Wheels::eCompletion Wheels::Right()
{
  Log("Wheels::Right");
  doEnable();
  return SetTarget(Motor::dirClockwise, Motor::dirCounterclockwise, 255);
}

Wheels::eCompletion Wheels::Forward()
{
  Log("Wheels::Forward");
  doEnable();
  return SetTarget(Motor::dirClockwise, Motor::dirClockwise, 255);
}

Wheels::eCompletion Wheels::Back()
{
  Log("Wheels::Back");
  doEnable();
  return SetTarget(Motor::dirCounterclockwise, Motor::dirCounterclockwise, 255);
}

Wheels::eCompletion Wheels::Stop()
{
  Log("Wheels::Stop");
  if (SetTarget(Motor::dirNone, Motor::dirNone, 0) == DONE)
  {
    doStandby();
    return DONE;
  }
  else
  {
    return STOPPING;
  }
}

void Wheels::Brake()
{
  Log("Wheels::Brake");
  _mA->Brake();
  _mB->Brake();
  doStandby();
}
