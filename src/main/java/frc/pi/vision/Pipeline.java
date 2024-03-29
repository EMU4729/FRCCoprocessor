package frc.pi.vision;

import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSink;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;

public class Pipeline implements VisionPipeline {
  private final int WIDTH = 160;
  private final int HEIGHT = 120;

  private final CvSink inputStream;
  private final CvSource outputStream;
  private final CvSource rawOutputStream;

  private final GenericEntry boxXEntry;
  private final GenericEntry boxYEntry;
  private final GenericEntry boxWEntry;
  private final GenericEntry boxHEntry;
  private final GenericEntry coneXEntry;
  private final GenericEntry coneYEntry;
  private final GenericEntry coneWEntry;
  private final GenericEntry coneHEntry;

  private Mat img;

  private Rect getBoxRect() {
    return new Rect(
        new Point(
            boxXEntry.get().getDouble(),
            boxYEntry.get().getDouble()),
        new Size(
            boxWEntry.get().getDouble(),
            boxHEntry.get().getDouble()));
  }

  private Rect getConeRect() {
    return new Rect(
        new Point(
            coneXEntry.get().getDouble(),
            coneYEntry.get().getDouble()),
        new Size(
            coneWEntry.get().getDouble(),
            coneHEntry.get().getDouble()));
  }

  private SimpleWidget configNumberSliderWidth(SimpleWidget widget) {
    return widget.withWidget(BuiltInWidgets.kNumberSlider)
        .withProperties(Map.of("min", 0, "max", WIDTH))
        .withSize(2, 1);
  }

  private SimpleWidget configNumberSliderHeight(SimpleWidget widget) {
    return widget.withWidget(BuiltInWidgets.kNumberSlider)
        .withProperties(Map.of("min", 0, "max", HEIGHT))
        .withSize(2, 1);
  }

  public Pipeline() {
    inputStream = CameraServer.getVideo();
    outputStream = CameraServer.putVideo("Gripper", WIDTH, HEIGHT);
    rawOutputStream = CameraServer.putVideo("Raw", WIDTH, HEIGHT);
    img = new Mat();

    ShuffleboardTab tab = Shuffleboard.getTab("Gripper Cam");

    tab.add(outputStream).withSize(4, 3).withPosition(0, 0);
    tab.add(rawOutputStream).withSize(4, 3).withPosition(4, 0);

    boxXEntry = configNumberSliderWidth(tab.add("Box X", 0.))
        .withPosition(0, 3).getEntry();
    boxYEntry = configNumberSliderHeight(tab.add("Box Y", 0.))
        .withPosition(2, 3).getEntry();
    boxWEntry = configNumberSliderWidth(tab.add("Box Width", 50.))
        .withPosition(4, 3).getEntry();
    boxHEntry = configNumberSliderHeight(tab.add("Box Height", 50.))
        .withPosition(6, 3).getEntry();
    coneXEntry = configNumberSliderWidth(tab.add("Cone X", 0.))
        .withPosition(0, 4).getEntry();
    coneYEntry = configNumberSliderHeight(tab.add("Cone Y", 0.))
        .withPosition(2, 4).getEntry();
    coneWEntry = configNumberSliderWidth(tab.add("Cone Width", 50.))
        .withPosition(4, 4).getEntry();
    coneHEntry = configNumberSliderHeight(tab.add("Cone Height", 50.))
        .withPosition(6, 4).getEntry();

    // this prevents cone height from breaking so don't remove it
    tab.add("The Answer", 42).withPosition(0, 5);

  }

  @Override
  public void process(Mat _mat) {
    if (inputStream.grabFrame(img) == 0) {
      outputStream.notifyError(inputStream.getError());
    }

    rawOutputStream.putFrame(img);

    // Box guide
    Rect boxRect = getBoxRect();
    Imgproc.rectangle(
        img,
        boxRect,
        new Scalar(255, 0, 0),
        1);

    // Cone guide
    Rect coneRect = getConeRect();
    Imgproc.line(
        img,
        new Point(
            coneRect.x,
            coneRect.y + coneRect.height),
        new Point(
            coneRect.x + coneRect.width,
            coneRect.y + coneRect.height),
        new Scalar(0, 255, 0),
        1);
    Imgproc.line(
        img,
        new Point(
            coneRect.x + coneRect.width / 2,
            coneRect.y),
        new Point(
            coneRect.x + coneRect.width / 2,
            coneRect.y + coneRect.height),
        new Scalar(0, 255, 0),
        1);

    outputStream.putFrame(img);
  }
}
