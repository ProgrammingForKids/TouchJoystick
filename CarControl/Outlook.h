#ifndef __ProgrammableCar__CarControl__Outlook_H__
#define __ProgrammableCar__CarControl__Outlook_H__

class Outlook
{
  String _name;
  const int _pinEcho;
  const int _pinTrig;
  const unsigned long _delay_for_range;
  const int _pinLed;

public:
  Outlook(String name, int echo, int trig, unsigned long range, int led=-1);
  void begin();
  bool isInRange();
};

#endif // !defined __ProgrammableCar__CarControl__Outlook_H__


