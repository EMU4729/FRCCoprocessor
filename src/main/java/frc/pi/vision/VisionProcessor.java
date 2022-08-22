package frc.pi.vision;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSink;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class VisionProcessor {
  private final int WIDTH = 640;
  private final int HEIGHT = 480;

  private final CvSink inputStream;
  private final CvSource analysisOutStream;
  private final CvSource rawOutStream;
  private final NetworkTableEntry targetXEntry;
  private final NetworkTableEntry targetYEntry;

  private Mat img;

  private List<Double> xList = new ArrayList<>();
  private List<Double> yList = new ArrayList<>();

  public VisionProcessor() {
    inputStream = CameraServer.getVideo();
    analysisOutStream = CameraServer.putVideo("Analyzed", WIDTH, HEIGHT);
    rawOutStream = CameraServer.putVideo("Raw", WIDTH, HEIGHT);

    NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();
    NetworkTable nt = ntInstance.getTable("Vision");
    targetXEntry = nt.getEntry("x");
    targetYEntry = nt.getEntry("y");
  }

  public void debug() {
    if (inputStream.grabFrame(img) == 0) {
      analysisOutStream.notifyError(inputStream.getError());
      return;
    }
    analysisOutStream.putFrame(img);
  }

  public void raw() {
    img = new Mat();
    if (inputStream.grabFrame(img) == 0) {
      rawOutStream.notifyError(inputStream.getError());
      return;
    }
    rawOutStream.putFrame(img);
  }

  public void analyze() {
    img = new Mat();
    if (inputStream.grabFrame(img) == 0) {
      analysisOutStream.notifyError(inputStream.getError());
      return;
    }

    // Convert to HSV and threshold image
    Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2HSV);
    Core.inRange(img,
        new Scalar(90, 0, 155),
        new Scalar(150, 255, 255),
        img);

    // Find all contours
    Mat _hierarchy = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(img, contours, _hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

    contours = contours.stream().filter(contour -> {
      // Ignore contours with low area (noise)
      if (Imgproc.contourArea(contour) < 15) {
        return false;
      }

      // Gets the rectangle surrounding the contour
      MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray()); // This is just to appease the compiler
      RotatedRect rect = Imgproc.minAreaRect(contour2f);

      Point center = rect.center;

      // Draw contour onto output
      Imgproc.circle(img, center, 3, new Scalar(0, 0, 255));

      // Add data points to output lists
      xList.add((center.x - WIDTH / 2) / (WIDTH / 2));
      yList.add((center.y - WIDTH / 2) / (WIDTH / 2));

      return true;
    }).collect(Collectors.toCollection(ArrayList::new));

    // Draw all contours
    Imgproc.drawContours(img, contours, -1, new Scalar(0, 0, 255));

    // Send output lists through NetworkTables
    targetXEntry.setDoubleArray(xList.stream().mapToDouble(i -> i).toArray());
    targetYEntry.setDoubleArray(yList.stream().mapToDouble(i -> i).toArray());

    // Send frame to output stream
    analysisOutStream.putFrame(img);
  }
}
