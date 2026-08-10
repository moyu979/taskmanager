package infra.connectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.Dimension;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.io.*;
import java.util.Random;

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
        System.out.println("connector 初始化成功");
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
        ProcessBuilder pb = new ProcessBuilder("adb", "-s", destLocater(), "exec-out", "screencap", "-p");
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
                "adb", "-s", destLocater(), "shell", "input", "tap", String.valueOf(x), String.valueOf(y));
        System.out.println("click"+x+","+y);
        Process process = pb.start();
        int exitCode = process.waitFor();
        System.out.println(exitCode);
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
        try {
            Dimension size = loadCurrentScreenSizeByWindowDisplays();

            if (size == null) {
                size = loadScreenSizeByWmSize();
            }

            if (size == null || size.width <= 0 || size.height <= 0) {
                throw new IOException("无法获取屏幕分辨率");
            }

            this.screenSize = size;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("adb 命令被中断", e);
        }
    }

    /**
     * 优先方式：
     * adb shell dumpsys window displays
     *
     * 常见输出里会有：
     * init=1080x2400 cur=2400x1080
     *
     * cur 就是当前屏幕方向下的尺寸，最适合 adb input tap。
     */
    private Dimension loadCurrentScreenSizeByWindowDisplays()
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "adb", "-s", destLocater(),
                "shell", "dumpsys", "window", "displays"
        );

        Process process = pb.start();

        Pattern curPattern = Pattern.compile("cur=(\\d+)x(\\d+)");
        Pattern appPattern = Pattern.compile("app=(\\d+)x(\\d+)");

        Dimension result = null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                Matcher curMatcher = curPattern.matcher(line);
                if (curMatcher.find()) {
                    int width = Integer.parseInt(curMatcher.group(1));
                    int height = Integer.parseInt(curMatcher.group(2));
                    result = new Dimension(width, height);
                    break;
                }

                // 某些 ROM 可能没有 cur=，但有 app=
                Matcher appMatcher = appPattern.matcher(line);
                if (appMatcher.find()) {
                    int width = Integer.parseInt(appMatcher.group(1));
                    int height = Integer.parseInt(appMatcher.group(2));
                    result = new Dimension(width, height);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return null;
        }

        return result;
    }

    /**
     * fallback：
     * adb shell wm size
     *
     * 注意：这个通常是物理尺寸，不一定代表当前横竖屏方向。
     */
    private Dimension loadScreenSizeByWmSize()
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "adb", "-s", destLocater(),
                "shell", "wm", "size"
        );

        Process process = pb.start();

        int width = -1;
        int height = -1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("Override size:")) {
                    String[] parts = line.substring("Override size:".length())
                            .trim()
                            .split("x");

                    width = Integer.parseInt(parts[0]);
                    height = Integer.parseInt(parts[1]);
                    break;
                }

                if (line.startsWith("Physical size:")) {
                    String[] parts = line.substring("Physical size:".length())
                            .trim()
                            .split("x");

                    width = Integer.parseInt(parts[0]);
                    height = Integer.parseInt(parts[1]);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0 || width <= 0 || height <= 0) {
            return null;
        }

        return new Dimension(width, height);
    }

}
