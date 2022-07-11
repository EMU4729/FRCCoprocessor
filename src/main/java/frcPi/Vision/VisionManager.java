package frcPi.Vision;

import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionThread;

import frcPi.Startup.ReadConfig;
import frcPi.Startup.StartCamera;
import frcPi.Utils.SwitchedCameraConfig;
import frcPi.Utils.CameraConfig;
import frcPi.Variables;

import org.opencv.core.Mat;

public class VisionManager {
  private static ReadConfig config = new ReadConfig();
  private static StartCamera camStart = new StartCamera();

  private static Variables vars = Variables.getInstance();

  public void startVisionThreads() {
    // read configuration
    if (!config.readConfig()) {
      return;
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

  /**
   * Example pipeline.
   */
  public static class MyPipeline implements VisionPipeline {
    public final VisionProcessor processor = new VisionProcessor();

    @Override
    public void process(Mat mat) {
      processor.analyze(mat);
    }
  }
}
