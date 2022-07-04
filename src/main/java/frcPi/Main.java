package frcPi;
// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionThread;

import frcPi.Startup.ReadConfig;
import frcPi.Startup.StartCamera;
import frcPi.Utils.SwitchedCameraConfig;
import frcPi.Utils.CameraConfig;

import org.opencv.core.Mat;

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
  private static ReadConfig Config = new ReadConfig();
  private static StartCamera camStart = new StartCamera();

  private static Variables vars = Variables.getInstance();
  public static EasyNetworkTableExample ez_b = new EasyNetworkTableExample();

  private Main() {
  }

  
  /**
   * Example pipeline.
   */
  public static class MyPipeline implements VisionPipeline {
    public int val;

    @Override
    public void process(Mat mat) {
      val += 1;
    }
  }

  /**
   * Main.
   */
  public static void main(String... args) {
    if (args.length > 0) {
      vars.configFile = args[0];
    }

    // read configuration
    if (!Config.readConfig()) {
      return;
    }

    // start NetworkTables
    NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
    if (vars.server) {
      System.out.println("Setting up NetworkTables server");
      ntinst.startServer();
    } else {
      System.out.println("Setting up NetworkTables client for team " + vars.team);
      ntinst.startClientTeam(vars.team);
      ntinst.startDSClient();
    }

    // start cameras
    for (CameraConfig config : vars.cameraConfigs) {
      vars.cameras.add(camStart.startCamera(config));
    }

    // start switched cameras
    for (SwitchedCameraConfig config : vars.switchedCameraConfigs) {
      camStart.startSwitchedCamera(config);
    }

    // start image processing on camera 0 if present
    if (vars.cameras.size() >= 1) {
      VisionThread visionThread = new VisionThread(vars.cameras.get(0),
              new MyPipeline(), pipeline -> {
        // do something with pipeline results
      });
      /* something like this for GRIP:
      VisionThread visionThread = new VisionThread(cameras.get(0),
              new GripPipeline(), pipeline -> {
        ...
      });
       */

      visionThread.start();
    }
    
    // loop forever
    for (;;) {
      ez_b.periodic();
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }
}
