#ifndef __ProgrammableCar__CarControl__TB6612FNG_H__
#define __ProgrammableCar__CarControl__TB6612FNG_H__

class TB6612FNG
{
  class Motor
  {
    const int _pin1 : 5;
    const int _pin2 : 5;
    const int _pinPWM : 5;
    const String _name;

    unsigned int _speed : 8;
    int _val1 : 1;
    int _val2 : 1;

    void Report(String op);

  public:
    Motor(String n, int p1, int p2, int pPWM);
    void begin();
    void Set(int targetSpeed);
    void Brake();
  };

  int _pinSTDBY;
  bool _bEnabled;

  Motor _mLeft;
  Motor _mRight;
  
  public:
    TB6612FNG(int pPWMA, int pINA2, int pINA1, int pSTDBY, int pINB1, int pINB2, int pPWMB);
    void begin();

    void Brake();
    void Go(unsigned short speedStep, unsigned short MaxSpeedStep, short leftFactor, short rightFactor);
    
  private:
    void doStandby();
    void doEnable();
};

#endif // !defined __ProgrammableCar__CarControl__TB6612FNG_H__
