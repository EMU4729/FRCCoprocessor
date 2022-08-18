package frc.pi;
// Copyright (c) FIRST and other WPILib contributors.

import frc.pi.utils.NetworkManager;
import frc.pi.vision.VisionManager;

/*
   JSON format:
   {
       "team": <team number>,
       "ntmode": <"client" or "server", "client" if unspecified>
       "cameras": [
           {
               "name": <camera name>
               "path": <path, e.g. "/dev/video0">
               "pixel format": <"MJPEG", "YUYV", etc>   // optional
               "width": <video mode width>              // optional
               "height": <video mode height>            // optional
               "fps": <video mode fps>                  // optional
               "brightness": <percentage brightness>    // optional
               "white balance": <"auto", "hold", value> // optional
               "exposure": <"auto", "hold", value>      // optional
               "properties": [                          // optional
                   {
                       "name": <property name>
                       "value": <property value>
                   }
               ],
               "stream": {                              // optional
                   "properties": [
                       {
                           "name": <stream property name>
                           "value": <stream property value>
                       }
                   ]
               }
           }
       ]
       "switched cameras": [
           {
               "name": <virtual camera name>
               "key": <network table key used for selection>
               // if NT value is a string, it's treated as a name
               // if NT value is a double, it's treated as an integer index
           }
       ]
   }
 */

public final class Main {
  private static VisionManager visManage = new VisionManager();
  private static NetworkManager _netManage = NetworkManager.getInstance();

  private static Variables vars = Variables.getInstance();

  private Main() {
  } // doesn't seem to do anything but leave it in

  /**
   * Main.
   */
  public static void main(String... args) {
    if (args.length > 0) {
      vars.configFile = args[0];
    }

    visManage.startVisionThreads();

    // loop forever
    for (;;) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }
}
