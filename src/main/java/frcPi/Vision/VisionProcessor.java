package frcPi.Vision;

import java.time.Instant;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSink;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class VisionProcessor {
  public final int WIDTH = 100;
  public final int HEIGHT = 100;

  public final CvSink inputStream;
  public final CvSource outputStream;
  public final NetworkTableEntry targetXEntry;
  public final NetworkTableEntry targetYEntry;

  public Mat inputImg = new Mat();
  public Mat outputImg = new Mat();

  public VisionProcessor() {
    CameraServer.startAutomaticCapture();
    inputStream = CameraServer.getVideo();
    outputStream = CameraServer.putVideo("Processed", WIDTH, HEIGHT);

    NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();
    NetworkTable nt = ntInstance.getTable("Vision");
    targetXEntry = nt.getEntry("x");
    targetYEntry = nt.getEntry("y");
  }

  public void analyze() {
    long startTime = Instant.now().toEpochMilli();

    long frameTime = inputStream.grabFrame(inputImg);

    // Notify output of error and skip iteration
    if (frameTime == 0) {
      outputStream.notifyError(inputStream.getError());
      return;
    }

    outputImg = inputImg.clone();

    // Convert to HSV and threshold image
    Imgproc.cvtColor(inputImg, inputImg, Imgproc.COLOR_BGR2HSV);
    Core.inRange(inputImg, new Scalar(65, 65, 200), new Scalar(85, 255, 255), inputImg);

    Mat _hierarchy = new Mat();
    ArrayList<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(inputImg, contours, _hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

    for (MatOfPoint contour : contours) {
      MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
      if (Imgproc.contourArea(contour) < 15) {
        continue;
      }

      RotatedRect rect = Imgproc.minAreaRect(contour2f);

      Point center = rect.center;
      Size size = rect.size;
      double angle = rect.angle;

      Mat boxPoints = new Mat();
      ArrayList<Mat> boxPointsList = new ArrayList<>();
      boxPointsList.add(boxPoints);

      Imgproc.drawContours(outputImg, boxPointsList, -1, new Scalar(0, 0, 255));

    }

  }
}
