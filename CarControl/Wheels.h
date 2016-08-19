#ifndef __ProgrammableCar__CarControl__WHEELS__H__
#define __ProgrammableCar__CarControl__WHEELS__H__


class Wheels
{
  protected:
    class Motor;

    Motor* mA;
    Motor* mB;

  protected:
    Wheels();

    virtual void doStandby() = 0;
    virtual void doEnable() = 0;

  public:
    void Left();
    void Right();
    void Forward();
    void Back();
    void Stop();
    void Brake();
    virtual void begin() = 0;
};

class Wheels::Motor
{
  protected:
    Motor() {}
  public:
    virtual void GoClockwise() = 0;
    virtual void GoCounterclockwise() = 0;
    virtual void Stop() = 0;
    virtual void Brake() = 0;
    virtual void begin() = 0;
};

#endif // !defined __ProgrammableCar__CarControl__WHEELS__H__
