package FileGetter;

import common.Area;
import cvFunc.TemplateMatching;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * 用于从制定路径加载位置信息的静态工具
 * */
public class LocationGetter {
    static public Area getlocation(Dimension screenSize, String path){
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
        Mat templateImg = Imgcodecs.imread(sourceFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        return TemplateMatching.matchTemplate(sourceImg,templateImg);

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
