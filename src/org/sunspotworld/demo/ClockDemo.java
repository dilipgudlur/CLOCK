/*
 * Copyright (c) 2006-2010 Sun Microsystems, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 **/

package org.sunspotworld.demo;

//import com.sun.cldc.jna.Spot;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.resources.transducers.SwitchEvent;
import com.sun.spot.service.BootloaderListenerService;
import java.util.Calendar;
import javax.microedition.midlet.MIDletStateChangeException;
import com.sun.spot.peripheral.*;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ISwitchListener;
import com.sun.spot.sensorboard.EDemoBoard;
/**
 * Clock Demo
 *
 * Deploy on a Spot with an eDemoBoard attached. When you shake the
 * Sun SPOT back and forth, the RGB LEDs will flash in a pattern
 * that will magically spell out words in the air.
 *
 * @author roger (modifications by vipul)
 */

public class ClockDemo extends javax.microedition.midlet.MIDlet implements ISwitchListener {
    Clock disp = new Clock();
    protected void startApp() throws MIDletStateChangeException {
        BootloaderListenerService.getInstance().start();       // Listen for downloads/commands over USB connection
        System.out.println("StartApp");
        

        ISwitch SW0      = EDemoBoard.getInstance().getSwitches()[0];
        ISwitch SW1      = EDemoBoard.getInstance().getSwitches()[1];

        //IAT91_TC timer   = (IAT91_TC)Spot.getInstance().getAT91_TC(4);
        
        boolean switch0Status = false;
        boolean switch1Status = false;
        
        SW0.addISwitchListener(this);
        SW1.addISwitchListener(this);
        // Main loop of the application
        while (true) {
           if(SW0.isOpen() && SW1.isOpen()){
            showTime();
        }         
        else {
            //Spot.getInstance().getPowerController().setTime(0);
            //disp.setColor(255, 0, 0);
            //disp.scroll("SET", 1);
            showResetTime();

            /*disp.setColor(0, 255, 0);
            disp.swingThis("P", 3);
            disp.setColor(0, 0, 255);
            disp.swingThis("O", 3);
            disp.setColor(0, 255, 0);
            disp.swingThis("T", 3);*/
            }
      
        }
    }

    public void showResetTime()
    {
        disp.setColor(255, 0, 0);
        disp.swingThis(Integer.toString(resetHour()), 12);
        disp.setColor(0, 0, 255);
        disp.swingThis(Integer.toString(resetMinute()), 12);
        disp.setColor(255, 0, 0);
        disp.swingThis(Integer.toString(resetSecond()), 12);
    }
    public void showTime()
    {
        disp.setColor(255, 0, 0);
        disp.swingThis(Integer.toString(getHour()), 12);
        disp.setColor(0, 0, 255);
        disp.swingThis(Integer.toString(getMinute()), 12);
        disp.setColor(255, 0, 0);
        disp.swingThis(Integer.toString(getSecond()), 12);
        disp.setColor(0, 255, 0);
        disp.swingThis(getAM_PM(), 12);
    }
    public int resetHour()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        int hour = Calendar.HOUR_OF_DAY;
        return hour;
    }
   
    public int resetMinute(){
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MINUTE, 0);
      int minute = cal.get(Calendar.MINUTE);
      return minute;
    }

    public int resetSecond()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        int second = Calendar.SECOND;
        return second;
    }

  public int getHour(){
      Calendar calendar = Calendar.getInstance();
      int hour = calendar.get(Calendar.HOUR);
      return hour;
  }

  public int getMinute(){
      Calendar calendar = Calendar.getInstance();
      int minute = calendar.get(Calendar.MINUTE);
      return minute;
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
  }

    protected void pauseApp() {
    }
    
    /**
     * Called if the MIDlet is terminated by the system.
     * I.e. if startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called.
     * 
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true when this method is called, the MIDlet must
     *    cleanup and release all resources. If false the MIDlet may throw
     *    MIDletStateChangeException  to indicate it does not want to be destroyed
     *    at this time.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        ITriColorLEDArray leds = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
        leds.setOff();
    }

    public void switchPressed(SwitchEvent evt) {

    }

    public void switchReleased(SwitchEvent evt) {
    }
}
