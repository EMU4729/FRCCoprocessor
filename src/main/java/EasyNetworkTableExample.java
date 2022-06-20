import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class EasyNetworkTableExample {
   NetworkTableEntry xEntry;
   NetworkTableEntry yEntry;

   public EasyNetworkTableExample() {
      //Get the default instance of NetworkTables that was created automatically
      //when your program starts
      NetworkTableInstance inst = NetworkTableInstance.getDefault();

      //Get the table within that instance that contains the data. There can
      //be as many tables as you like and exist to make it easier to organize
      //your data. In this case, it's a table called datatable.
      NetworkTable table = inst.getTable("datatable");

      //Get the entries within that table that correspond to the X and Y values
      //for some operation in your program.
      xEntry = table.getEntry("X");
      yEntry = table.getEntry("Y");
   }

   double x = 0;
   double y = 0;

   public void periodic() {
      //Using the entry objects, set the value to a double that is constantly
      //increasing. The keys are actually "/datatable/X" and "/datatable/Y".
      //If they don't already exist, the key/value pair is added.
      System.out.println(xEntry.getValue().getDouble() +" "+ yEntry.getValue().getDouble());
   }
}