package FileGetter;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.io.File;

/**
 * 用于从制定位置，加载图片的静态工具，主要是用于加载匹配模版
 */
public class MatGetter {
    static public Mat getlocation(Dimension screenSize, String path){
        /*
         * 计划：直接根据文件夹读取，看有没有，有的话返回mat，否则返回null
         * */
        ;
        String fileName = screenSize.width + "_" + screenSize.height + ".png";
        File file = new File(path, fileName);

        if (!file.exists()) {
            return null;
        }

        Mat img = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        return img.empty() ? null : img;
    }
}
