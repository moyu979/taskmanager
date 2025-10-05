package cvFunc;

import common.Area;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class TemplateMatching {
    static double matchTresh=0.8;

    /**
     * 执行模板匹配，返回匹配区域的上下左右坐标
     *
     * @param source   原图（Mat）
     * @param template 模板图（Mat）
     * @return 匹配区域边界：top, bottom, left, right
     */
    public static Area matchTemplate(Mat source, Mat template) {
        if (source.empty() || template.empty()) {
            throw new IllegalArgumentException("图像或模板为空");
        }

        int resultCols = source.cols() - template.cols() + 1;
        int resultRows = source.rows() - template.rows() + 1;
        Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

        Imgproc.matchTemplate(source, template, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;

        if (mmr.maxVal < matchTresh) {
            return null; // 匹配度不够，返回 null
        }

        int left = (int) matchLoc.x;
        int top = (int) matchLoc.y;
        int right = left + template.cols();
        int bottom = top + template.rows();

        Area area=new Area(left,top,right-left,bottom-top);
        return area;
    }
}
