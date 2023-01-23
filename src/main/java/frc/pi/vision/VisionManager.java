package frc.pi.vision;

import edu.wpi.first.vision.VisionThread;
import frc.pi.Variables;
import frc.pi.startup.ReadConfig;
import frc.pi.startup.StartCamera;
import frc.pi.structures.CameraConfig;
import frc.pi.structures.SwitchedCameraConfig;

public class VisionManager {
  private static ReadConfig config = new ReadConfig();
  private static StartCamera camStart = new StartCamera();
  private static Variables vars = Variables.getInstance();

  public void startVisionThreads() {
    if (!config.readConfig())
      return;

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
          new Pipeline(), pipeline -> {
          });
      /*
       * something like this for GRIP:
       * VisionThread visionThread = new VisionThread(cameras.get(0),
       * new GripPipeline(), pipeline -> {
       * ...
       * });
       */
      visionThread.start();
    }
  }
}
