package frc.pi.utils;

import java.util.Optional;

import frc.pi.Variables;

public class Logger {
  private static Optional<Logger> instance = Optional.empty();
  private Variables values = Variables.getInstance();

  private Logger() {
  }

  public static Logger getIntstance() {
    if (instance.isEmpty()) {
      instance = Optional.of(new Logger());
    }
    return instance.get();
  }

  /**
   * Report parse error.
   */
  public void parseError(String str) {
    System.err.println("config error in '" + values.configFile + "': " + str);
  }
}
