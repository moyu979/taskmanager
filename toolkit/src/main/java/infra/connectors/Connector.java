package infra.connectors;

import org.opencv.core.Mat;

import java.awt.*;
import java.io.IOException;

public interface Connector {
    Mat screenCap() throws IOException;
    void leftClick(int x, int y) throws IOException, InterruptedException;
    void rightClick(int x, int y) throws IOException, InterruptedException;
    void longPress(int x, int y, int timeMs) throws IOException, InterruptedException;
    void backGesture() throws IOException, InterruptedException;
    Dimension getScreenSize();
    void loadScreenSize() throws IOException;

}
