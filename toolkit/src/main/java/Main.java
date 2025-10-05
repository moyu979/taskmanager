import FileGetter.LocationGetter;
import FileGetter.MatGetter;
import common.Area;
import connectors.ADBConnector;
import connectors.Connector;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.awt.*;
import java.io.IOException;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        Connector connector=new ADBConnector("123");
        Toolkits toolkits=new Toolkits(connector,"./");
        toolkits.setNowDir("test");
        toolkits.rightClickArea("main/entry");
    }
}

