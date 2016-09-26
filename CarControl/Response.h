#ifndef _ProgrammingForKids_TouchJoystick__Response_h__                                                                                                                  
#define _ProgrammingForKids_TouchJoystick__Response_h__                                                                                                                  

#include "Log.h"

class Response                                                                                                                                                          
{
public:
  enum eSpeed
  {
    FullRev = 0,
    SlowRev = 1,
    Idle = 2,
    SlowFwd = 3,
    FullFwd = 4
  };
  
  struct HeadObstacle{ static const byte val = 0x01 << 7; };
  struct TailObstacle{ static const byte val = 0x01 << 6; };

private:
  unsigned short _obstacle1 : 1; // MSB
  unsigned short _obstacle2 : 1;

  eSpeed _left : 3; // left motor speed (see enum)                                                                                                                                             
  eSpeed _right : 3;// right motor speed (see enum)

  bool _isSet;

public:
  Response()
  : _isSet(false)
  {
  }

  Response(HeadObstacle)
  : _obstacle1(1)
  , _obstacle2(0)
  , _left(0)
  , _right(0)
  , _isSet(true)
  {
  }

  Response(TailObstacle)
  : _obstacle1(0)
  , _obstacle2(1)
  , _left(0)
  , _right(0)
  , _isSet(true)
  {
  }
 
  Response(HeadObstacle, TailObstacle)
  : _obstacle1(1)
  , _obstacle2(1)
  , _left(0)
  , _right(0)
  , _isSet(true)
  {
  }
  
  Response(short left, short right)
  : _obstacle1(0)
  , _obstacle2(0)
  , _left(left+2)
  , _right(right+2)
  , _isSet(true)
  {
  }

  operator const byte() const
  {
    byte ret = ( HeadObstacle::val * _obstacle1 );
    ret |= ( TailObstacle::val * _obstacle2 );
    ret |= (_left & 0x07) << 3;
    ret |= (_right & 0x07);
    return ret;
  }

  bool isObstacle() const
  {
    return (0 != (_obstacle1 | _obstacle2));
  }

  bool isSet() const
  {
    return _isSet;
  }

  void ToLog() const
  {
    if (_isSet)
    {
      Log("Response ")(static_cast<byte>(*this), HEX)(" [Obstacle1=")(_obstacle1)("] [Obstacle2=")(_obstacle2)("] [Left=")(_left)("] [Right=")(_right)(']');
    }
    else
    {
      Log("Reposnse not set");
    }
  }
};                                                                                                                                                                      
                                                                                                                                                                        
#endif // _ProgrammingForKids_TouchJoystick__Response_h__                                                                                                                
