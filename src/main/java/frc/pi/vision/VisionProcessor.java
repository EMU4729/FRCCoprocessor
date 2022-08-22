package frc.pi.vision;

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

  public VisionProcessor() {
    inputStream = CameraServer.getVideo();
    analysisOutStream = CameraServer.putVideo("Analyzed", WIDTH, HEIGHT);
    rawOutStream = CameraServer.putVideo("Raw", WIDTH, HEIGHT);

    NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();
    NetworkTable nt = ntInstance.getTable("Vision");
    targetXEntry = nt.getEntry("x");
    targetYEntry = nt.getEntry("y");
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
    Core.inRange(img, new Scalar(0, 0, 200), new Scalar(180, 50, 255), img);

    // Find all contours
    Mat _hierarchy = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(img, contours, _hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

    if (contours.size() == 0) {
      return;
    }

    MatOfPoint contour = contours.stream().reduce(contours.get(0), (max, current) -> {
      if (Imgproc.contourArea(current) > Imgproc.contourArea(max)) {
        return current;
      } else {
        return max;
      }
    });

    MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray()); // This is just to appease the compiler
    RotatedRect rect = Imgproc.minAreaRect(contour2f);

    // Draw the contour
    Imgproc.rectangle(img, rect.boundingRect(), new Scalar(255, 0, 0));

    // Send data through NetworkTables
    Point center = rect.center;
    targetXEntry.setNumber((center.x - WIDTH / 2) / (WIDTH / 2));
    targetYEntry.setNumber((center.y - WIDTH / 2) / (WIDTH / 2));

    // Send frame to output stream
    analysisOutStream.putFrame(img);
  }
}
