package connectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.*;
import java.io.*;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ADBConnector implements Connector{
    private String ip;
    private String port;
    private String deviceId;

    @JsonIgnore
    private String adbPath="adb.exe";
    @JsonIgnore
    private Dimension screenSize;
    @JsonIgnore
    private Random random;

    @JsonCreator
    public ADBConnector(
            @JsonProperty("ip") String ip,
            @JsonProperty("port") String port,
            @JsonProperty("deviceID") String deviceID
    ) {
        init(ip, port, deviceID);
    }

    public ADBConnector(String ip, String port) {
        init(ip, port, null);
    }

    public ADBConnector(String deviceId) {
        init(null, null, deviceId);
    }

    private void init(String ip, String port, String deviceId) {
        this.ip = ip;
        this.port = port;
        this.deviceId = deviceId;
        this.random=new Random();

        try {
            loadScreenSize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Mat screenCap() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(adbPath, "-s", destLocater(), "exec-out", "screencap", "-p");
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
                adbPath, "-s", destLocater(), "shell", "input", "tap", String.valueOf(x), String.valueOf(y));

        Process process = pb.start();
        int exitCode = process.waitFor();
    }

    public void rightClick(int x, int y) throws IOException, InterruptedException {
        this.longPress(x,y,2);
    }
    /**
     * 获取目标设备标识
     * 优化：如果 IP+Port 和 deviceId 都为空，直接抛出异常，避免 ProcessBuilder 报错
     */
    private String destLocater() {
        if (this.deviceId != null && !this.deviceId.isEmpty()) {
            return deviceId;
        } else if (this.ip != null && !this.ip.isEmpty() && this.port != null && !this.port.isEmpty()) {
            return this.ip + ":" + this.port;
        } else {
            throw new IllegalStateException("必须提供 deviceId 或 IP+Port 才能执行 ADB 操作");
        }
    }

    /**
     * 长按操作
     * adb shell input swipe x1 y1 x2 y2 duration
     * 通过 x1==x2, y1==y2 模拟长按
     */
    public void longPress(int x, int y, int timeMs) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                adbPath, "-s", destLocater(), "shell", "input", "swipe",
                String.valueOf(x), String.valueOf(y),
                String.valueOf(x), String.valueOf(y),
                String.valueOf(timeMs)
        );
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("adb longPress 命令执行失败");
        }
    }

    @Override
    public void backGesture() throws IOException, InterruptedException {
        // 起点 X 坐标（左边缘 0~30）
        int startX = random.nextInt(31); // 0~30

        // 终点 X 坐标（向右滑 250~400 px）
        int endX = 250 + random.nextInt(151); // 250~400

        // Y 坐标随机化：屏幕中间 40%~60%
        int minY = (int) (screenSize.height * 0.4);
        int maxY = (int) (screenSize.height * 0.6);
        int y = minY + random.nextInt(maxY - minY + 1);

        // 滑动时长 80~150 ms
        int duration = 80 + random.nextInt(71); // 80~150

        ProcessBuilder pb = new ProcessBuilder(
                adbPath, "-s", destLocater(), "shell", "input", "swipe",
                String.valueOf(startX), String.valueOf(y),
                String.valueOf(endX), String.valueOf(y),
                String.valueOf(duration)
        );

        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("adb backGesture failed");
        }
    }



    @Override
    public Dimension getScreenSize() {
        return this.screenSize;
    }

    /**
     * 优化 loadScreenSize，确保 adb 命令执行完成
     */
    public void loadScreenSize() throws IOException {
        // 1. 获取物理分辨率
        ProcessBuilder pbSize = new ProcessBuilder("adb", "-s", destLocater(), "shell", "wm", "size");
        Process processSize = pbSize.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processSize.getInputStream()))) {
            String line;
            int width = -1, height = -1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Physical size:")) {
                    String[] parts = line.substring("Physical size:".length()).trim().split("x");
                    width = Integer.parseInt(parts[0]);
                    height = Integer.parseInt(parts[1]);
                }
            }
            int exitCode = processSize.waitFor();
            if (exitCode != 0) {
                throw new IOException("adb wm size 命令执行失败");
            }
            if (width <= 0 || height <= 0) throw new IOException("无法获取屏幕分辨率");

            // 2. 获取屏幕方向
            ProcessBuilder pbOri = new ProcessBuilder("adb", "-s", destLocater(), "shell", "dumpsys", "input");
            Process processOri = pbOri.start();
            int orientation = 0;
            try (BufferedReader readerOri = new BufferedReader(new InputStreamReader(processOri.getInputStream()))) {
                while ((line = readerOri.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("SurfaceOrientation:")) {
                        orientation = Integer.parseInt(line.split(":")[1].trim());
                        break;
                    }
                }
            }
            int exitCodeOri = processOri.waitFor();
            if (exitCodeOri != 0) {
                throw new IOException("adb dumpsys input 命令执行失败");
            }

            // 3. 根据方向调整宽高
            if (orientation == 1 || orientation == 3) {
                int tmp = width;
                width = height;
                height = tmp;
            }
            this.screenSize = new Dimension(width, height);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("adb 命令被中断", e);
        }
    }

}
