package frc.pi.structures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CameraConfig {
  public String name;
  public String path;
  public JsonObject config;
  public JsonElement streamConfig;
}
