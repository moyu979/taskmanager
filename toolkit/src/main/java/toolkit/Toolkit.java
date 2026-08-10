package toolkit;

import infra.FileGetter.LocationGetter;
import infra.FileGetter.MatGetter;
import common.Area;
import infra.connectors.Connector;
import org.opencv.core.Rect;
import utils.cvFunc.ExistAny;
import utils.cvFunc.OCRUtils;
import utils.cvFunc.TemplateMatching;
import org.opencv.core.Mat;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Toolkit {

    private Connector connector;

    private String rootDir; // 运行程序的根目录
    private String nowDir; //偏置目录1，一般是游戏名
    private Random random;

    public Toolkit(Connector connector, String rootDir){
        this.connector=connector;
        this.rootDir=rootDir;
        random=new Random();
    }

    public void sleep(int seconds){
        double scale = 1.0 + random.nextDouble() * 0.2; // [1.0, 1.2)
        long sleepMillis = (long) (seconds * 1000 * scale);

        try {
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            System.err.println("Sleep interrupted: " + e.getMessage());
        }
    }

    public String getNowDir() {
        return nowDir;
    }

    public void setNowDir(String nowDir) {
        this.nowDir = nowDir;
    }

    public void leftClickArea(String path) throws IOException, InterruptedException {
        Area area= LocationGetter.getLocation(connector.getScreenSize(),getRealPath(path));
        Point point=area.randomPoint();
        connector.leftClick(point.x,point.y);
    }

    public void leftClickMat(String path) throws IOException, InterruptedException {
        Mat templateMat= MatGetter.getMat(connector.getScreenSize(),getRealPath(path));
        Mat screenMat=connector.screenCap();
        Area area= TemplateMatching.matchTemplate(screenMat,templateMat);
        Point point=area.randomPoint();
        connector.leftClick(point.x,point.y);
    }

    public void rightClickArea(String path) throws IOException, InterruptedException {
        Area area= LocationGetter.getLocation(connector.getScreenSize(),getRealPath(path));
        Point point=area.randomPoint();
        connector.rightClick(point.x,point.y);
    }

    public void rightClickMat(String path) throws IOException, InterruptedException {
        Mat templateMat= MatGetter.getMat(connector.getScreenSize(),getRealPath(path));
        Mat screenMat=connector.screenCap();
        Area area= TemplateMatching.matchTemplate(screenMat,templateMat);
        Point point=area.randomPoint();
        connector.rightClick(point.x,point.y);
    }

    public void backGesture() throws IOException, InterruptedException {
        connector.backGesture();
    }

    public ArrayList<Boolean> existAny(ArrayList<String> dsts) throws IOException {
        Mat source=connector.screenCap();
        ArrayList<Mat> dstMat=new ArrayList<>();
        for(int i=0;i<dsts.size();i++){
            dstMat.add(MatGetter.getMat(connector.getScreenSize(),getRealPath(dsts.get(i))));
        }
        return ExistAny.exist(source,dstMat);
    }
    /**
     * 判断一张模板图片是否在当前截图中存在
     *
     * @param dst 模板图片路径（相对于 nowDir）
     * @return true 表示图中存在该模板，false 表示不存在
     */
    public boolean exist(String dst) throws IOException {
        Mat source = connector.screenCap();
        Mat template = MatGetter.getMat(connector.getScreenSize(), getRealPath(dst));

        if (template == null) {
            return false;
        }

        Area area = TemplateMatching.matchTemplate(source, template);
        return area != null;
    }

    /**
     * 根据配置路径获取位置区域，截屏后只对该区域进行 OCR 识别
     *
     * @param path 配置路径（相对于 nowDir）
     * @return 识别到的文字
     */
    public String extractText(String path) throws IOException {
        // 1. 用 LocationGetter 获取区域
        Area area = LocationGetter.getLocation(connector.getScreenSize(), getRealPath(path));
        if (area == null) {
            return "";
        }

        // 2. 截屏
        Mat screen = connector.screenCap();

        // 3. 裁剪出 area 区域
        Rect roi = new Rect(area.getX(), area.getY(), area.getWidth(), area.getHeight());
        Mat cropped = new Mat(screen, roi);

        // 4. OCR 识别（复用 infra.ocr.OCRUtils）
        String result = OCRUtils.extractText(cropped);

        // 5. 释放资源
        cropped.release();
        screen.release();

        return result;
    }

    private String getRealPath(String relativePath){
        return rootDir+"/"+nowDir+"/"+relativePath;
    }

    public static void test(){
        System.out.println("a test");
    }

    public Dimension loadScreenSize(){
        return connector.getScreenSize();
    }

}
