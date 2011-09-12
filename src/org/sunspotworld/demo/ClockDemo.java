
package org.sunspotworld.demo;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.resources.transducers.SwitchEvent;
import com.sun.spot.service.BootloaderListenerService;
import java.util.Calendar;
import javax.microedition.midlet.MIDletStateChangeException;
import com.sun.spot.peripheral.*;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ISwitchListener;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
/**
 * Clock Demo
 */

public class ClockDemo extends javax.microedition.midlet.MIDlet implements ISwitchListener{
    Clock disp = new Clock();
    ISwitch SW0;
    ISwitch SW1;
    boolean setTimeFlag = false;
    static int hod;
    static int minute;
    boolean changeMinuteFlag;
    boolean changeHourFlag;
    boolean oneTimeFlag = false;
    Calendar cal, cal1;
    long start,end,startOffset,endOffset,deltaOffset;
    private static final int INACTIVE = 0;
    private static final int PRESSED = 1;
    private static final int RELEASED = 2;
    private static int postMsgStatus = INACTIVE;

    protected void startApp() throws MIDletStateChangeException {
        BootloaderListenerService.getInstance().start();       // Listen for downloads/commands over USB connection
        System.out.println("StartApp");
        cal = Calendar.getInstance();
        SW0      = EDemoBoard.getInstance().getSwitches()[0];
        SW1      = EDemoBoard.getInstance().getSwitches()[1];
        SW0.addISwitchListener(this);
        SW1.addISwitchListener(this);
        hod  = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        showCurrentTime();
    }

    public void showCurrentTime()
    {
        //cal.set(Calendar.HOUR_OF_DAY, hod);
        //cal.set(Calendar.MINUTE, minute);
        System.out.println("in show current time");
        while (SW0.isOpen())
            renderTime();
     }

    public void renderTime()
    {
        System.out.println("in render time");
        Date d = new Date();
        endOffset = d.getTime();
        //System.out.println("STARTOFFSET1="+startOffset/1000);
        //System.out.println("ENDOFFSET1="+endOffset/1000);
        deltaOffset = endOffset - startOffset;
        if(oneTimeFlag){
           System.out.println("in render time");
           Calendar cal2 = Calendar.getInstance();
           cal2.set(Calendar.HOUR_OF_DAY, hod);
           cal2.set(Calendar.MINUTE,minute);
           long baseTime = cal2.getTime().getTime();
           deltaOffset = baseTime + deltaOffset;
        }
        d.setTime(deltaOffset);
        cal1 = Calendar.getInstance();
        cal1.setTime(d);
        //System.out.println("DATE"+d);
        //System.out.println("DELTAOFFSET="+deltaOffset/1000);
        System.out.println(cal1.get(Calendar.HOUR_OF_DAY));
        System.out.println(cal1.get(Calendar.MINUTE));
        System.out.println(cal1.get(Calendar.SECOND));

        disp.setColor(255, 0, 0);
        disp.swingThis(Integer.toString(cal1.get(Calendar.HOUR_OF_DAY)), 12);
        disp.setColor(0, 255, 0);
        disp.swingThis(Integer.toString(cal1.get(Calendar.MINUTE)), 12);
        disp.setColor(0, 0, 255);
        disp.swingThis(Integer.toString(cal1.get(Calendar.SECOND)), 12);
        /*if(disp.accelZ()){
            System.out.println("required accel");
            disp.setColor(0,255,0);      //leds blink green for settime mode
            setTimeFlag = true;
            oneTimeFlag = true;
            setTimeMode();               //we are in set time mode //set new time
        }*/
    }

    protected void pauseApp() {
    }
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        ITriColorLEDArray leds = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
        leds.setOff();
    }

    public void switchPressed(SwitchEvent evt) {

        if(evt.getSwitch().equals(SW0)){ //check condition...switchPressedFlag may not get true
            System.out.println("in switch pressed");
            start = System.currentTimeMillis();
           }
        if (evt.getSwitch().equals(SW1)) {
           if (postMsgStatus == INACTIVE) {
                postMsgStatus = PRESSED;
              try{
                 Thread.sleep(10000);
              } catch (InterruptedException ex) {
                    ex.printStackTrace();
              }
              if (postMsgStatus == PRESSED) {
                  long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
                  String postURL = getAppProperty("POST-URL");
                  String msg = IEEEAddress.toDottedHex(ourAddr) + " Achieving world peace ";
                  postMessage(postURL, msg);
                  postMsgStatus = INACTIVE;
               }
           }
        }
    }

    public void switchReleased(SwitchEvent evt) {
       if (evt.getSwitch().equals(SW1)) {
            if (postMsgStatus == PRESSED) {
                postMsgStatus = RELEASED;
            }
        }

       if(evt.getSwitch().equals(SW0)){  //setTimeFlag controls access to checking delta
           System.out.println("in switch released");
           end = System.currentTimeMillis();
           long delta = (end-start)/1000;
           if(delta >= 3) //switch 1 has been pressed for more than 3 sec
           {
               disp.setColor(0,255,0);      //leds blink green for settime mode
               setTimeFlag = true;
               oneTimeFlag = true;
               setTimeMode();               //we are in set time mode //set new time
               System.out.println("1");
           }
           else            //switch1 is just a click less than 3 sec
           {
               if(setTimeFlag && changeMinuteFlag)     //minutes have to be incremented
               {
                   incrementMinute();
                   System.out.println("2");}
               else if( setTimeFlag && changeHourFlag)
               {
                   incrementHourOfDay(); //hours have to be incremented
                   System.out.println("3");}
               }
        }
    }

    public void setTimeMode()
    {
        System.out.println("in process time");
        Date d=new Date();
        startOffset = d.getTime();
        hod = 0;
        minute = 0;
        cal.set(Calendar.HOUR_OF_DAY, hod);
        cal.set(Calendar.MINUTE, minute);
        while(SW1.isOpen()) //press both SW0 and SW1 together to exit this while loop
        {
            if(SW0.isOpen())
                SW0.waitForChange();
            SW0.waitForChange();
            System.out.println("click detected..changing hour");
            changeHourFlag = true;
        }
        SW1.waitForChange();
        System.out.println("reach this stage");
        while(SW1.isOpen())
        {
          System.out.println("inside minute change");
          if(SW0.isOpen())
                SW0.waitForChange();
            SW0.waitForChange();
            changeMinuteFlag = true;
        }
        System.out.println("outside setting time");
        setTimeFlag = false;
        changeMinuteFlag = false;
        changeHourFlag = false;
        disp.setColor(0,255,0);
        Utils.sleep(3000);

        showCurrentTime();
    }

    public void incrementHourOfDay()
    {
        System.out.println("in increment hour");
        hod++;
        if(hod >= 24)
            hod = 0;
        cal.set(Calendar.HOUR_OF_DAY, hod);
    }
    public void incrementMinute()
    {
        System.out.println("in increment minute");
        minute++;
        if(minute >= 60)
            minute = 0;
        cal.set(Calendar.MINUTE, minute);
    }

     public static void postMessage(String postURL, String msg) {
        HttpConnection conn = null;
        OutputStream out = null;
        String resp = null;

        System.out.println("Posting: <" + msg + "> to " + postURL);

        try {
            conn = (HttpConnection) Connector.open(postURL);
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Connection", "close");
       /* conn.setRequestProperty("token", "4e826530-6ff2-48a7-8cd3-75b98ff0d7fd");*/
            conn.setRequestProperty("content-type", "text/plain");
            /*conn.setRequestProperty("data", "Checking posting to twitter: Divya");*/
            out = conn.openOutputStream();
            out.write((msg + "'\n").getBytes());
            out.flush();
        } catch (Exception ex) {
            resp = ex.getMessage();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (IOException ex) {
                resp = ex.getMessage();
            }
        }
        System.out.flush();
        ITriColorLEDArray leds = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
        leds.setColor(LEDColor.YELLOW);
        leds.setOn();
    }
}

/*public void showResetTime()
    {
        disp.setColor(255, 0, 0);
//        disp.swingThis(Integer.toString(resetHour()), 12);
//        disp.setColor(0, 0, 255);
//        disp.swingThis(Integer.toString(resetMinute()), 12);
//        disp.setColor(255, 0, 0);
//        disp.swingThis(Integer.toString(resetSecond()), 12);
        disp.swingThis("SET", 12);
        disp.setColor(0, 0, 255);
        disp.swingThis("TIME", 12);
        //disp.setColor(255, 0, 0);
        //disp.swingThis(Integer.toString(resetSecond()), 12);
    }


    public int resetSecond()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        int second = Calendar.SECOND;
        return second;
    }
 *
  /*public int getHourOfDay(){
      Calendar calendar = Calendar.getInstance();
      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      return hour;

  }

  public int getMinute(){
      Calendar calendar = Calendar.getInstance();
      int min = calendar.get(Calendar.MINUTE);
      return min;
  }
 public void resetHour()
    {

        cal.set(Calendar.HOUR_OF_DAY, hod);
    }

    public void resetMinute(){

      cal.set(Calendar.MINUTE, minute);
   }

 public int getSecond(){
      Calendar calendar = Calendar.getInstance();
      int second = calendar.get(Calendar.SECOND);
      return second;
  }

  public String getAM_PM(){
      Calendar calendar = Calendar.getInstance();
      String am_pm;
      if(calendar.get(Calendar.AM_PM) == 0)
      am_pm = "AM";
      else
      am_pm = "PM";
      return am_pm;
  }*/

