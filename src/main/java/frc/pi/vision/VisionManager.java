package frc.pi.vision;

import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionThread;
import frc.pi.startup.ReadConfig;
import frc.pi.startup.StartCamera;
import frc.pi.utils.CameraConfig;
import frc.pi.utils.SwitchedCameraConfig;
import frc.pi.Variables;

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

  /**
   * Example pipeline.
   */
  public static class Pipeline implements VisionPipeline {
    public final VisionProcessor processor = new VisionProcessor();

    @Override
    public void process(Mat _mat) {
      long tmp1 = Runtime.getRuntime().freeMemory();
      processor.raw();
      long tmp2 = Runtime.getRuntime().freeMemory();
      processor.analyze();
      System.out.println(tmp1 + "  " + tmp2 + "  " + Runtime.getRuntime().freeMemory());
    }
  }
}
