#ifndef __ProgrammableCar__CarControl__Outlook_H__
#define __ProgrammableCar__CarControl__Outlook_H__

class Outlook
{
  const int pinEcho;
  const int pinTrig;
  const unsigned long delay_for_range;

public:
  Outlook(int echo, int trig, unsigned long range);
  void begin();
  bool isInRange();
};

#endif // !defined __ProgrammableCar__CarControl__Outlook_H__


