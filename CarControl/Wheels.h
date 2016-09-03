#ifndef __ProgrammableCar__CarControl__WHEELS__H__
#define __ProgrammableCar__CarControl__WHEELS__H__


class Wheels
{
  protected:
    class Motor
    {
      protected:
        Motor() {}
      public:
        enum eDir { dirNone, dirClockwise, dirCounterclockwise };
    
        virtual eDir Direction() const = 0;
        virtual int Speed() const = 0;
    
        virtual void Set(eDir targetDir, int targetSpeed) = 0;
        virtual void Brake() = 0;
    
        virtual void begin() = 0;
    };

    Motor* _mA;
    Motor* _mB;

  protected:
    Wheels();

    virtual void doStandby() = 0;
    virtual void doEnable() = 0;

  public:
    //  Motion functions signal the driver to start execution of the command
    //  Return value indicates the stage of the task
    //  If the wheels were running in another direction they first stop. This is STOPPING.
    //  Then, except for the Stop() commant, the wheels are STOPPED, then STARTING in the new direction
    //  Finally the state is DONE which means the engines are rotating
    //  in proper direction with full speed.
    //  Stop() goes from STOPPING directly to DONE
    enum eCompletion { STOPPING, STOPPED, STARTING, DONE };
    
    eCompletion Left();
    eCompletion Right();
    eCompletion Forward();
    eCompletion Back();
    eCompletion Stop();
    void Brake();
    virtual void begin() = 0;

  private:
      eCompletion SetTarget(Motor::eDir targetDirA, Motor::eDir targetDirB, int targetSpeed);
};

#endif // !defined __ProgrammableCar__CarControl__WHEELS__H__
