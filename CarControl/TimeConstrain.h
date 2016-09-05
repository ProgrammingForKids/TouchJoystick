#ifndef __ProgrammableCar__CarControl__TimeConstrain_H__
#define __ProgrammableCar__CarControl__TimeConstrain_H__

#include "Log.h"

class TimeConstrain
{
  unsigned long       _threshold;
  String              _name;
  
public:
  TimeConstrain(String name)
  : _threshold(millis())
  , _name(name)
  {
  }

  void set(unsigned long period)
  {
    _threshold = period + millis();
    Log("Constrain ")(_name)(" set to +")(period);
  }

  bool check() const
  {
    return _threshold < millis();
  }
};

#endif // ! #defined __ProgrammableCar__CarControl__TimeConstrain_H__
