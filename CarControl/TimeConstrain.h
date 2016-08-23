#ifndef __ProgrammableCar__CarControl__TimeConstrain_H__
#define __ProgrammableCar__CarControl__TimeConstrain_H__

class TimeConstrain
{
  unsigned long       _threshold;

public:
  TimeConstrain()
  : _threshold(millis())
  {
  }

  void set(unsigned long period)
  {
    _threshold = period + millis();
  }

  bool check() const
  {
    return _threshold < millis();
  }
};

#endif // ! #defined __ProgrammableCar__CarControl__TimeConstrain_H__
