import FileGetter.LocationGetter;
import FileGetter.MatGetter;
import common.Area;
import connectors.ADBConnector;
import connectors.Connector;
import cvFunc.TemplateMatching;
import org.opencv.core.Mat;

import java.awt.*;
import java.io.IOException;

public class Toolkits {

    private Connector connector;

    private String rootDir;
    private String nowDir;

    public Toolkits(Connector connector,String rootDir){
        this.connector=connector;
        this.rootDir=rootDir;
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
        Mat templateMat= MatGetter.getLocation(connector.getScreenSize(),getRealPath(path));
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
        Mat templateMat= MatGetter.getLocation(connector.getScreenSize(),getRealPath(path));
        Mat screenMat=connector.screenCap();
        Area area= TemplateMatching.matchTemplate(screenMat,templateMat);
        Point point=area.randomPoint();
        connector.rightClick(point.x,point.y);
    }

    public void backGesture() throws IOException, InterruptedException {
        connector.backGesture();
    }

    private String getRealPath(String relativePath){
        return rootDir+"/"+nowDir+"/"+relativePath;
    }



}
