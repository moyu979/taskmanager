package connectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.*;
import java.io.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ADBConnector implements Connector{
    private String ip;
    private String port;

    @JsonIgnore
    private String adbPath="adb.exe";
    @JsonIgnore
    private Dimension screenSize;

    @JsonCreator
    public ADBConnector(
            @JsonProperty("ip") String ip,
            @JsonProperty("port") String port) {
        this.ip = ip;
        this.port = port;

        // 反序列化完成后执行初始化逻辑
        try {
            loadScreenSize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Mat screenCap() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(adbPath, "-s", this.port, "exec-out", "screencap", "-p");
        Process process = pb.start();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = process.getInputStream();
        byte[] buffer = new byte[4096];
        int len;

        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }

        // 等待进程完成
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("adb screencap failed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        byte[] imageBytes = outputStream.toByteArray();

        // 使用 OpenCV 解码 PNG 数据为 Mat 格式
        Mat img = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
        if (img.empty()) {
            throw new IOException("Failed to decode image from adb screencap");
        }

        return img;
    }

    public void leftClick(int x, int y) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                adbPath, "-s", this.port, "shell", "input", "tap", String.valueOf(x), String.valueOf(y));

        Process process = pb.start();
        int exitCode = process.waitFor();
    }

    public void rightClick(int x, int y) throws IOException {
        this.longPress(x,y,2);
    }

    public void longPress(int x, int y, int time) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(adbPath, "-s", this.port, "shell", "input", "swipe",
                String.valueOf(x),String.valueOf(x));
        Process process = pb.start();
    }
    

    /**
     * 获取当前屏幕方向下的分辨率 (width, height)
     */
    public void loadScreenSize() throws IOException {
        // 1. 获取物理分辨率
        ProcessBuilder pbSize = new ProcessBuilder("adb", "-s", this.port, "shell", "wm", "size");

        Process processSize = pbSize.start();
        int width = -1, height = -1;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processSize.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Physical size:")) {
                    String[] parts = line.substring("Physical size:".length()).trim().split("x");
                    width = Integer.parseInt(parts[0]);
                    height = Integer.parseInt(parts[1]);
                }
            }
        }

        if (width <= 0 || height <= 0) throw new IOException("无法获取屏幕分辨率");

        // 2. 获取屏幕方向
        ProcessBuilder pbOri = new ProcessBuilder("adb", "-s", this.port, "shell", "dumpsys", "input");

        Process processOri = pbOri.start();
        int orientation = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processOri.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("SurfaceOrientation:")) {
                    orientation = Integer.parseInt(line.split(":")[1].trim());
                    break;
                }
            }
        }

        // 3. 根据方向调整宽高
        if (orientation == 1 || orientation == 3) {
            int tmp = width;
            width = height;
            height = tmp;
        }
        this.screenSize=new Dimension(width,height);
    }
}
