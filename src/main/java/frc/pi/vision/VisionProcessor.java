package frc.pi.vision;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class VisionProcessor {
  private final int WIDTH = 640;
  private final int HEIGHT = 480;

  private final CvSource outputStream;
  private final NetworkTableEntry targetXEntry;
  private final NetworkTableEntry targetYEntry;

  private Mat outputImg;

  private List<Double> xList = new ArrayList<>();
  private List<Double> yList = new ArrayList<>();

  public VisionProcessor() {
    outputStream = CameraServer.putVideo("Processed", WIDTH, HEIGHT);

    NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();
    NetworkTable nt = ntInstance.getTable("Vision");
    targetXEntry = nt.getEntry("x");
    targetYEntry = nt.getEntry("y");
  }

  public void debug(Mat inputImg) {
    outputStream.putFrame(inputImg);
  }

  public void analyze(Mat inputImg) {
    long startTime = Instant.now().toEpochMilli();

    outputImg = inputImg.clone();

    // Convert to HSV and threshold image
    Imgproc.cvtColor(inputImg, inputImg, Imgproc.COLOR_BGR2HSV);
    Core.inRange(inputImg, new Scalar(65, 65, 200), new Scalar(85, 255, 255), inputImg);

    // Find all contours
    Mat _hierarchy = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(inputImg, contours, _hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

    for (MatOfPoint contour : contours) {
      // This is just to appease the compiler
      MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());

      // Ignore contours with low area (noise)
      if (Imgproc.contourArea(contour) < 15) {
        continue;
      }

      // Gets the rectangle surrounding the contour
      RotatedRect rect = Imgproc.minAreaRect(contour2f);

      Point center = rect.center;

      Mat boxPoints = new Mat();
      List<MatOfPoint> boxPointsList = new ArrayList<>();
      boxPointsList.add(new MatOfPoint(boxPoints));

      // Draw contour onto output
      Imgproc.drawContours(outputImg, boxPointsList, -1, new Scalar(0, 0, 255));
      Imgproc.circle(outputImg, center, 3, new Scalar(0, 0, 255));

      // Add data points to output lists
      xList.add((center.x - WIDTH / 2) / (WIDTH / 2));
      yList.add((center.y - WIDTH / 2) / (WIDTH / 2));
    }

    // Send output lists through NetworkTables
    targetXEntry.setDoubleArray(xList.stream().mapToDouble(i -> i).toArray());
    targetYEntry.setDoubleArray(yList.stream().mapToDouble(i -> i).toArray());

    // Calculate and display FPS
    long processingTime = Instant.now().toEpochMilli() - startTime;
    double fps = 1 / processingTime;
    Imgproc.putText(outputImg, String.valueOf((int) Math.round(fps)), new Point(0, 40), Imgproc.FONT_HERSHEY_SIMPLEX, 1,
        new Scalar(255, 255, 255));

    // Send frame to output stream
    outputStream.putFrame(outputImg);
  }
}
