package frc.pi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.wpi.first.cscore.VideoSource;
import frc.pi.utils.CameraConfig;
import frc.pi.utils.SwitchedCameraConfig;

public class Variables {
  private static Optional<Variables> instance = Optional.empty();

  private Variables() {
  }

  public static Variables getInstance() {
    if (instance.isEmpty()) {
      instance = Optional.of(new Variables());
    }
    return instance.get();
  }

  public String configFile = "/boot/frc.json";

  public int team;
  public boolean server;
  public List<CameraConfig> cameraConfigs = new ArrayList<>();
  public List<SwitchedCameraConfig> switchedCameraConfigs = new ArrayList<>();
  public List<VideoSource> cameras = new ArrayList<>();
}
