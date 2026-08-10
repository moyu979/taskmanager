package utils.cvFunc;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class ExistAny {
    /**
     *
     * @param mats
     * @return
     */
    public static ArrayList<Boolean> exist(Mat source,ArrayList<Mat> mats){
        ArrayList<Boolean> result=new ArrayList<>();
        for(int i=0;i< mats.size();i++){
            result.add(TemplateMatching.matchTemplate(source,mats.get(i))!=null);
        }
        return result;
    }
}
