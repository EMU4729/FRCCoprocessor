package frc.pi.utils;

import java.util.Optional;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import frc.pi.Variables;

public class NetworkManager {
  private static Optional<NetworkManager> inst = Optional.empty();

  private NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
  private Variables vars = Variables.getInstance();

  private NetworkManager() {
    startNetwork();
  }

  public static NetworkManager getInstance() {
    if (inst.isEmpty()) {
      inst = Optional.of(new NetworkManager());
    }
    return inst.get();
  }

  private void startNetwork() {
    if (vars.server) {
      System.out.println("Setting up NetworkTables server");
      ntinst.startServer();
    } else {
      System.out.println("Setting up NetworkTables client for team " + vars.team);
      ntinst.startClient4("coprocessor");
      ntinst.setServerTeam(vars.team);
      ntinst.startDSClient();
    }
  }

  private double getDouble(GenericEntry e) {
    return e.get().getDouble();
  }

  private boolean getBoolean(GenericEntry e) {
    return e.get().getBoolean();
  }

  private String getString(GenericEntry e) {
    return e.get().getString();
  }

  private double[] getArray(GenericEntry e) {
    return e.get().getDoubleArray();
  }
}
