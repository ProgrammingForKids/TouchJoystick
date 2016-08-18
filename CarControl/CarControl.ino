const unsigned long MAX_DISTANCE = 35;
const unsigned long MAX_DURATION = MAX_DISTANCE*1000*1000*2/100/340;

#include <SoftwareSerial.h>// import the serial library


class Wheels
{
protected:
  class Motor
  {
  protected:
    Motor() {}
  public:
    virtual void GoClockwise() = 0;
    virtual void GoCounterclockwise() = 0;
    virtual void Stop() = 0;
    virtual void Brake() = 0;
  };
  
  Motor* mA;
  Motor* mB;

protected:
  Wheels()
  {
    mA = NULL;
    mB = NULL;
  }

  virtual void doStandby() = 0;
  virtual void doEnable() = 0;

public:
  void Left()
  {
    Serial.println("Wheels::Left");
    doEnable();
    mA->GoCounterclockwise();
    mB->GoClockwise();
  }

  void Right()
  {
    Serial.println("Wheels::Right");
    doEnable();
    mA->GoClockwise();
    mB->GoCounterclockwise();
  }
  
  void Forward()
  {
    Serial.println("Wheels::Forward");
    doEnable();
    mA->GoClockwise();
    mB->GoClockwise();    
  }
  
  void Back()
  {
    Serial.println("Wheels::Back");
    doEnable();
    mA->GoCounterclockwise();
    mB->GoCounterclockwise();
  }
  
  void Stop()
  {
    Serial.println("Wheels::Stop");
    mA->Stop();
    mB->Stop();
    doStandby();
  }
  
  void Brake()
  {
    Serial.println("Wheels::Brake");    
    mA->Stop();
    mB->Stop();
    doStandby();
  }
};

class TB6612FNG : public Wheels
{
  class TBMotor : public Wheels::Motor
  {
    const int pin1;
    const int pin2;
    const int pinPWM;
    const String name;
    
    int speed;
    int val1;
    int val2;

    int NextSpeed()
    {
      static const int Step = 32;
      static const int initSpeed = 128;
      
      if (speed == 255)
      {
        return 255;
      }

      if (speed < initSpeed)
      {
        return initSpeed;
      }

      if (speed + Step > 255)
        return 255;

      return speed + Step;
    }

    void Rotate(int p1, int p2)
    {
      if (p1 != val1 || p2 != val2)
      {
        if (speed != 0)
        {
          Stop();
          delay(10);
        }
      }

      speed = NextSpeed();
      val1 = p1;
      val2 = p2;
      analogWrite(pinPWM, speed);
      digitalWrite(pin1, p1);
      digitalWrite(pin2, p2);
    }

    void Report(String op)
    {
      Serial.print("Engine ");
      Serial.print(name);
      Serial.print(" going ");
      Serial.print(op);
      Serial.print(" -- ");
      Serial.print(val1);
      Serial.print(':');
      Serial.print(val2);
      Serial.print(" with speed ");
      Serial.println(speed);      
    }
    
  public:
    TBMotor(String n, int p1, int p2, int pPWM)
    : pin1(p1)
    , pin2(p2)
    , pinPWM(pPWM)
    , name(n)
    {
      pinMode(pin1, OUTPUT);
      pinMode(pin2, OUTPUT);
      pinMode(pinPWM, OUTPUT);
      speed=0;
      val1 = LOW;
      val2 = LOW;
    }

    void GoClockwise()
    {
      Rotate(HIGH, LOW);
      Report("Clockwise");
    }
    void GoCounterclockwise()
    {
      Rotate(LOW, HIGH);
      Report("Counterclockwise");
    }
    void Stop()
    {
      digitalWrite(pin1, LOW);
      digitalWrite(pin2, LOW);
      speed = 0;
      val1 = LOW;
      val2 = LOW;
      Report("Stop");
    }
    void Brake()
    {
      analogWrite(pinPWM, 0);      
      speed = 0;
      val1 = LOW;
      val2 = LOW;
      Report("Brake");
    }
  };

  int pinSTDBY;
  
public:
  TB6612FNG()
  {
  }

  void begin(int pPWMA, int pINA2, int pINA1, int pSTDBY, int pINB1, int pINB2, int pPWMB)
  {
    mA = new TBMotor("A", pINA1, pINA2, pPWMA);
    mB = new TBMotor("B", pINB1, pINB2, pPWMB);
    pinSTDBY = pSTDBY;
    pinMode(pinSTDBY, OUTPUT);
    digitalWrite(pinSTDBY, LOW);
  }

protected:
  void doStandby()
  {
    digitalWrite(pinSTDBY, LOW);
    Serial.println("doStandby");
  }
  void doEnable()
  {
    digitalWrite(pinSTDBY, HIGH);    
    Serial.println("doEnable");
  }
};

/*
Connections:
Motor driver
- Pin 3 ---> PWMA
- Pin 4 ---> AIN2
- Pin 5 ---> AIN1
- Pin 6 ---> STBY
- Pin 7 ---> BIN1
- Pin 8 ---> BIN2
- Pin 9 ---> PWMB

- Motor 1: A01 and A02
- Motor 2: B01 and B02

*/

//Define the Pins

//Motor 1
int pinPWMA = 3; //Speed
int pinAIN2 = 4; //Direction
int pinAIN1 = 5; //Direction

//Standby
int pinSTBY = 6;

//Motor 2
int pinBIN1 = 7; //Direction
int pinBIN2 = 8; //Direction
int pinPWMB = 9; //Speed

//Constants to help remember the parameters
static boolean turnCW = 0;  //for motorDrive function
static boolean turnCCW = 1; //for motorDrive function
static boolean motor1 = 0;  //for motorDrive, motorStop, motorBrake functions
static boolean motor2 = 1;  //for motorDrive, motorStop, motorBrake functions

// Bluetooth
int pinTx = 12; // purple
int pinRx = 2; // orange
// ground - blk
// vcc - white, gray - ground

//Ultrasonic sensor
int pinEcho = 10; // yellow
int pinTrig = 11; // green
// blue - vcc

int pinLed=13;


#define USE_SERIAL_MONITOR

void Delay(int ms)
{
#ifdef USE_SERIAL_MONITOR
  Serial.print("Delay ");
  Serial.print(ms);
  Serial.println(" ms");
#endif
  delay(ms);  
}



int HIGH_LIMIT=255;
int LOW_LIMIT=130;
int STEP=15;

SoftwareSerial BT(pinTx, pinRx);

TB6612FNG wheels;


void setup()
{
//Set the PIN Modes
/*  

  pinMode(pinPWMA, OUTPUT);
  pinMode(pinAIN1, OUTPUT);
  pinMode(pinAIN2, OUTPUT);

  pinMode(pinPWMB, OUTPUT);
  pinMode(pinBIN1, OUTPUT);
  pinMode(pinBIN2, OUTPUT);

  pinMode(pinSTBY, OUTPUT);
*/
  wheels.begin(pinPWMA, pinAIN2, pinAIN1, pinSTBY, pinBIN1, pinBIN2, pinPWMB);
  
#ifdef USE_SERIAL_MONITOR
  Serial.begin(9600);
#endif

  //configure pin modes
  pinMode(pinLed, OUTPUT);
  digitalWrite(pinLed, LOW);
  

  pinMode(pinTrig, OUTPUT);
  pinMode(pinEcho, INPUT);

  BT.begin(38400);
  BT.println("Bluetooth is Ready");

  Serial.println("Ready");

}



bool isTooClose()
{
  long duration;

  // The sensor is triggered by a HIGH pulse of 10 or more microseconds.
  // Give a short LOW pulse beforehand to ensure a clean HIGH pulse:
  digitalWrite(pinTrig, LOW);
  delayMicroseconds(5);
  digitalWrite(pinTrig, HIGH);
  delayMicroseconds(10);
  digitalWrite(pinTrig, LOW);
 
  // Read the signal from the sensor: a HIGH pulse whose
  // duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  duration = pulseIn(pinEcho, HIGH, MAX_DURATION*10);

 // Serial.println(duration);

  if (duration == 0)
    return false;
  if (duration > MAX_DURATION )
    return false;
  return true;
}

int speed = 0;

bool obstacle = false;
char  recent_state='s'; // Stopped

void loop()
{
   bool isClose = isTooClose();
   
   if ( (!obstacle) && isClose )
   {
      digitalWrite(pinLed, HIGH);
      BT.println("Obstacle! Emergency stop");
      obstacle = true;

      wheels.Brake();
   }


   if ( (!isClose) && obstacle)
   {
      digitalWrite(pinLed, LOW);
      BT.println("Obstacle removed");
      obstacle = false;    
   }
   
   int BluetoothData='s';
   
   if (BT.available())
   {
      BluetoothData=BT.read();
      Serial.print("Received ");
      Serial.println((char)BluetoothData);
   }
   
    char report='\0';
    
    if (recent_state != BluetoothData)
    {
      wheels.Stop();
        report='S';
        delay(100);
    }

    recent_state = BluetoothData;

    int aftermath = 30;
    
    switch (BluetoothData)
    {
      case 'f':
        if (obstacle)
        {
          BT.println("Cant go forward, obstacle");
          wheels.Brake();
         recent_state='s';
        }
        else
        {
          wheels.Forward();
          recent_state='f';
          report = 'F';
          aftermath = 100;
        }
        break;

      case 'b':
        wheels.Back();
        recent_state='b';
        report = 'B';
        aftermath = 100;
        break;

      case 'l':
        wheels.Left();
        recent_state='l';
        report = 'L';
        aftermath = 100;
        break;

      case 'r':
        wheels.Right();
        recent_state='r';
        report = 'R';
        aftermath = 100;
        break;

      case 's':
        break;
 
      default:
        Serial.println("Unknown command");
        report = 'X';
        break;
    }

     if (report != '\0')
     {
      BT.print(report);
     }
     
     delay(aftermath);// prepare for next data ...
}

