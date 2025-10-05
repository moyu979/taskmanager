package FileGetter;

import common.Area;
import cvFunc.TemplateMatching;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.Dimension;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 * 用于从制定路径加载位置信息的静态工具
 * 目前版本查完了，基本上能用
 * */
public class LocationGetter {
    static public Area getLocation(Dimension screenSize, String path){
        /*
        * 计划：先读取文件夹下的json或者xml文件，
        *   如果存在对应的位置，直接新建Area返回，
        *   如果不存在，寻找有没有对应的图像文件，
        *       如果存在，解析，并附加到文件后面，然后将文件刷回
        *       如果不存在，考虑报错
        * */
        File sourceFile = new File(path, screenSize.width + "_" + screenSize.height + "_source.png");
        File templateFile = new File(path, screenSize.width + "_" + screenSize.height + "_template.png");
        if (!sourceFile.exists() || !templateFile.exists()) {
            return null;
        }

        Mat sourceImg = Imgcodecs.imread(sourceFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        Mat templateImg = Imgcodecs.imread(templateFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        return TemplateMatching.matchTemplate(sourceImg,templateImg);

    }

    public static Area getAndShow(Dimension screenSize, String path) {
        File sourceFile = new File(path, screenSize.width + "_" + screenSize.height + "_source.png");
        File templateFile = new File(path, screenSize.width + "_" + screenSize.height + "_template.png");

        Mat sourceImg = Imgcodecs.imread(sourceFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        Mat templateImg = Imgcodecs.imread(templateFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);

        // 调用原有匹配逻辑
        Area matchedArea = TemplateMatching.matchTemplate(sourceImg, templateImg);

        // 在源图上绘制矩形框
        Point topLeft = new Point(matchedArea.getX(), matchedArea.getY());
        Point bottomRight = new Point(
                matchedArea.getX() + matchedArea.getWidth(),
                matchedArea.getY() + matchedArea.getHeight()
        );
        Imgproc.rectangle(sourceImg, topLeft, bottomRight, new Scalar(0, 0, 255), 2);

        // --- 自动缩放显示 ---
        double scaleX = (double) screenSize.width / sourceImg.width();
        double scaleY = (double) screenSize.height / sourceImg.height();
        double scale = Math.min(scaleX, scaleY); // 保持纵横比

        Mat displayImg = new Mat();
        Imgproc.resize(sourceImg, displayImg, new Size(), scale, scale);

        // 转换为 BufferedImage
        BufferedImage bufferedImage = (BufferedImage) HighGui.toBufferedImage(displayImg);

        // 用 Swing 显示可缩放大图
        ImageIcon icon = new ImageIcon(bufferedImage);
        JLabel label = new JLabel(icon);
        JScrollPane scrollPane = new JScrollPane(label);

        JFrame frame = new JFrame("Match Result");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(scrollPane);
        frame.setSize(screenSize.width, screenSize.height); // 初始窗口大小
        frame.setLocationRelativeTo(null); // 居中
        frame.setVisible(true);

        return matchedArea;
    }



    static public Map<Dimension,Area> loadxml(String path){
        //根据路径加载对应的json
        //这个在之前的版本没用cache，后面既然考虑到用商业的方案写，看看怎么加个cacheable
        ;
        return Map.of();
    }

    static public void writeBack(String path){
        //将原本的数据刷回；
    }

}
