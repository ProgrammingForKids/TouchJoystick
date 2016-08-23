#ifndef __ProgrammableCar__CarControl__WHEELS__H__
#define __ProgrammableCar__CarControl__WHEELS__H__


class Wheels
{
  protected:
    class Motor;

    Motor* _mA;
    Motor* _mB;

  protected:
    Wheels();

    virtual void doStandby() = 0;
    virtual void doEnable() = 0;

  public:
    //  Motion functions signal the driver to start execution of the command
    //  Return value indocates whether the target state has been reached
    //  For example if Left() returns true that means the engines are rotating
    //  in proper direction with full speed.
    bool Left();
    bool Right();
    bool Forward();
    bool Back();
    bool Stop();
    bool Brake();
    virtual void begin() = 0;
};

class Wheels::Motor
{
  protected:
    Motor() {}
  public:
    virtual bool GoClockwise() = 0;
    virtual bool GoCounterclockwise() = 0;
    virtual bool Stop() = 0;
    virtual bool Brake() = 0;
    virtual void begin() = 0;
};

#endif // !defined __ProgrammableCar__CarControl__WHEELS__H__
