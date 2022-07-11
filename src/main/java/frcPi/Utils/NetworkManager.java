package frcPi.Utils;

import java.util.Optional;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import frcPi.Variables;

public class NetworkManager {
  private static Optional<NetworkManager> inst;

  private NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
  private Variables vars = Variables.getInstance();

  private NetworkManager(){
    startNetwork();
  }
  public static NetworkManager getInstance(){
    if(inst.isEmpty()){
      inst = Optional.of(new NetworkManager());
    }
    return inst.get();
  }

  private void startNetwork(){
    if (vars.server) {
      System.out.println("Setting up NetworkTables server");
      ntinst.startServer();
    } else {
      System.out.println("Setting up NetworkTables client for team " + vars.team);
      ntinst.startClientTeam(vars.team);
      ntinst.startDSClient();
    }    
  }
  
  private double getDouble(NetworkTableEntry e){
    return e.getValue().getDouble();
  }
  private boolean getBoolean(NetworkTableEntry e){
    return e.getValue().getBoolean();
  }
  private String getString(NetworkTableEntry e){
    return e.getValue().getString();
  }
  private double[] getArray(NetworkTableEntry e){
    return e.getValue().getDoubleArray();
  }
}
