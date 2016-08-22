#include "TB6612FNG.h"

class TB6612FNG::TBMotor : public Wheels::Motor
{
    static const int Step = 32;
    static const int initSpeed = 128;

    const int _pin1;
    const int _pin2;
    const int _pinPWM;
    const String _name;

    int _speed;
    int _val1;
    int _val2;

    int SpeedStepUp()
    {
      if (_speed == 255)
      {
        return 255;
      }

      if (_speed < initSpeed)
      {
        return initSpeed;
      }

      if (_speed + Step > 255)
      {
        return 255;
      }
      
      return _speed + Step;
    }

    int SpeedStepDown()
    {
      if (_speed <= initSpeed)
      {
        return 0;
      }      
      else
      {
        return _speed - Step;
      }
    }

    bool Rotate(int p1, int p2)
    {
      
      if (p1 != _val1 || p2 != _val2)
      {
        if (_speed != 0)
        {
          Stop();
          return false;
        }
      }

      _speed = SpeedStepUp();
      _val1 = p1;
      _val2 = p2;
      analogWrite(_pinPWM, _speed);
      digitalWrite(_pin1, p1);
      digitalWrite(_pin2, p2);
      return _speed == 255;
    }

    void Report(String op)
    {
      Serial.print("Engine ");
      Serial.print(_name);
      Serial.print(" going ");
      Serial.print(op);
      Serial.print(" -- ");
      Serial.print(_val1);
      Serial.print(':');
      Serial.print(_val2);
      Serial.print(" with speed ");
      Serial.println(_speed);
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
    
    bool GoClockwise()
    {
      bool retval = Rotate(HIGH, LOW);
      Report("Clockwise");
      return retval;
    }
    
    bool GoCounterclockwise()
    {
      bool retval = Rotate(LOW, HIGH);
      Report("Counterclockwise");
      return retval;
    }
    
    bool Stop()
    {
      bool retval = true;
      
      if (_speed > 0)
      {
        _speed = SpeedStepDown();
        if (_speed > 0)
          analogWrite(_pinPWM, _speed);
        retval = false;
      }

      if (_speed == 0)
      {
        if (_val1 != LOW)
        {
          digitalWrite(_pin1, LOW);
          _val1 = LOW;
          retval = false;
        }
        if (_val2 != LOW)
        {
          digitalWrite(_pin2, LOW);
          _val2 = LOW;
          retval = false;
        }
      }
      Report("Stop");
      return retval;
    }

    bool Brake()
    {
      analogWrite(_pinPWM, 0);
      digitalWrite(_pin1, LOW);
      digitalWrite(_pin2, LOW);
      _speed = 0;
      _val1 = LOW;
      _val2 = LOW;
      Report("Brake");
      return true;
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
}

void TB6612FNG::doStandby()
{
  digitalWrite(_pinSTDBY, LOW);
  Serial.println("doStandby");
}

void TB6612FNG::doEnable()
{
  digitalWrite(_pinSTDBY, HIGH);
  Serial.println("doEnable");
}
