#ifndef __ProgrammableCar__CarControl__TB6612FNG_H__
#define __ProgrammableCar__CarControl__TB6612FNG_H__

#include "Wheels.h"

class TB6612FNG : public Wheels
{
  class TBMotor;

  int pinSTDBY;
  
public:
  TB6612FNG();
  void begin(int pPWMA, int pINA2, int pINA1, int pSTDBY, int pINB1, int pINB2, int pPWMB);

protected:
  void doStandby();
  void doEnable();
};

#endif // !defined __ProgrammableCar__CarControl__TB6612FNG_H__
