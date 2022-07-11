package frcPi.Startup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.MjpegServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.cscore.VideoSource;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTableInstance;

import frcPi.Utils.SwitchedCameraConfig;
import frcPi.Utils.CameraConfig;
import frcPi.Variables;

public class StartCamera {
  private Variables vars = Variables.getInstance();

  /**
   * Start running the camera.
   */
  public VideoSource startCamera(CameraConfig config) {
    System.out.println("Starting camera '" + config.name + "' on " + config.path);
    UsbCamera camera = new UsbCamera(config.name, config.path);
    MjpegServer server = CameraServer.startAutomaticCapture(camera);

    Gson gson = new GsonBuilder().create();

    camera.setConfigJson(gson.toJson(config.config));
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);

    if (config.streamConfig != null) {
      server.setConfigJson(gson.toJson(config.streamConfig));
    }

    return camera;
  }

  /**
   * Start running the switched camera.
   */
  public MjpegServer startSwitchedCamera(SwitchedCameraConfig config) {
    System.out.println("Starting switched camera '" + config.name + "' on " + config.key);
    MjpegServer server = CameraServer.addSwitchedCamera(config.name);

    NetworkTableInstance.getDefault()
        .getEntry(config.key)
        .addListener(event -> {
          if (event.value.isDouble()) {
            int i = (int) event.value.getDouble();
            if (i >= 0 && i < vars.cameras.size()) {
              server.setSource(vars.cameras.get(i));
            }
          } else if (event.value.isString()) {
            String str = event.value.getString();
            for (int i = 0; i < vars.cameraConfigs.size(); i++) {
              if (str.equals(vars.cameraConfigs.get(i).name)) {
                server.setSource(vars.cameras.get(i));
                break;
              }
            }
          }
        },
            EntryListenerFlags.kImmediate | EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);

    return server;
  }

}
