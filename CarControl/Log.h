#ifndef __ProgrammableCar__CarControl__Log_H_
#define __ProgrammableCar__CarControl__Log_H_

#define USE_SERIAL_PRINT



#ifdef USE_SERIAL_PRINT
class Log
{
public:
  static void begin() { Serial.begin(115200); }

  Log()
  {
    Serial.print('@');
    Serial.print(millis());
    Serial.print(": ");
  }

  Log(const String s)
  : Log()
  {
    Serial.print(s);
  }

  template<typename T>
  Log& operator()(T s)
  {
    Serial.print(s);
    return *this;
  }
  
  template<typename T, typename FMT>
  Log& operator()(T s, FMT f)
  {
    Serial.print(s,f);
    return *this;
  }

  ~Log()
  {
    Serial.println(' ');
  }
};
#else

class Log
{
public:
  Log(){};
  Log(String) {};
  template <typename _T> Log& operator()(_T) {return *this;}
  template <typename _T, typename _FMT> Log& operator()(_T, _FMT) {return *this;}
  static void begin() {}
};

#endif // USE_SERIAL_PRINT

#endif // !defined __ProgrammableCar__CarControl__Log_H_
