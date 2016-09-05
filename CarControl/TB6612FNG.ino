#include "TB6612FNG.h"
#include "Log.h"

class TB6612FNG::TBMotor : public Wheels::Motor
{
    const int _pin1;
    const int _pin2;
    const int _pinPWM;
    const String _name;

    int _speed;
    int _val1;
    int _val2;

    void Report(String op)
    {
      Log("Engine ")(_name)(" going ")(op)(" -- ")(_val1)(':')(_val2)(" with speed ")(_speed);
    }

  public:
    TBMotor(String n, int p1, int p2, int pPWM)
      : _pin1(p1)
      , _pin2(p2)
      , _pinPWM(pPWM)
      , _name(n)
    {
      _speed = 0;
      _val1 = LOW;
      _val2 = LOW;
    }

    void begin()
    {
      pinMode(_pin1, OUTPUT);
      pinMode(_pin2, OUTPUT);
      pinMode(_pinPWM, OUTPUT);
    }
    
    eDir Direction() const
    {
      if (_val1 == HIGH)
      {
        if (_val2 == LOW)
          return dirClockwise;
        else
          return dirNone;
      }
      else
      {
        if (_val2 == LOW)
          return dirNone;
        else
          return dirCounterclockwise;        
      }
    }

    virtual int Speed() const
    {
      return _speed;
    }
    
    virtual void Set(eDir targetDir, int targetSpeed)
    {
      String opname;
      
      switch (targetDir)
      {
      case dirClockwise:
        _val1 = HIGH;
        _val2 = LOW;
        opname = "Clockwise";
        break;

      case dirCounterclockwise:
        _val1 = LOW;
        _val2 = HIGH;
        opname = "Counterclockwise";
        break;

      case dirNone:
        _val1 = LOW;
        _val2 = LOW;
        opname = "Stop";
        break;
      }
      _speed = targetSpeed;

      analogWrite(_pinPWM, _speed);
      digitalWrite(_pin1, _val1);
      digitalWrite(_pin2, _val2);
      Report(opname);
    }


    void Brake()
    {
      analogWrite(_pinPWM, 0);
      digitalWrite(_pin1, LOW);
      digitalWrite(_pin2, LOW);
      _speed = 0;
      _val1 = LOW;
      _val2 = LOW;
      Report("Brake");
    }
};

TB6612FNG::TB6612FNG(int pPWMA, int pINA2, int pINA1, int pSTDBY, int pINB1, int pINB2, int pPWMB)
{
  _mA = new TBMotor("A", pINA1, pINA2, pPWMA);
  _mB = new TBMotor("B", pINB1, pINB2, pPWMB);
  _pinSTDBY = pSTDBY;
}

void TB6612FNG::begin()
{
  _mA->begin();
  _mB->begin();
  pinMode(_pinSTDBY, OUTPUT);
  digitalWrite(_pinSTDBY, LOW);
  _bEnabled = false;
}

void TB6612FNG::doStandby()
{
  if ( _bEnabled )
  {
    digitalWrite(_pinSTDBY, LOW);
    _bEnabled = false;
  }
  Log("doStandby");
}

void TB6612FNG::doEnable()
{
  if ( ! _bEnabled )
  {
    digitalWrite(_pinSTDBY, HIGH);
    _bEnabled = true;
  }
  Log("doEnable");
}
