import infra.connectors.ADBConnector;
import infra.connectors.Connector;
import nu.pattern.OpenCV;
import toolkit.Toolkit;
import java.io.IOException;

import static infra.ocr.OCRUtils.tesseract;

public class Main {
    static {
        try {
            OpenCV.loadLocally();
            System.setProperty("jna.library.path", "/opt/homebrew/lib");
            tesseract.setDatapath("/opt/homebrew/share/tessdata");
            tesseract.setLanguage("chi_sim");
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Connector connector=new ADBConnector("byjnr4654hnbhizh");
        connector.loadScreenSize();
        System.out.println(connector.getScreenSize());
        Toolkit toolkit=new Toolkit(connector,"/Users/chenhongxu/Desktop/blhx_manager/pics");
        toolkit.setNowDir("BLHX");
//        toolkit.leftClickMat("打开app");
        String s=toolkit.extractText("章节界面/章节编号");
        System.out.println(s);
        String page_id=toolkit.extractText("章节界面/章节编号").replace("第",
                "").replace("章","").trim();
        while(Integer.parseInt(page_id)!=8){
            if(Integer.parseInt(page_id)>8){
                toolkit.leftClickArea("章节界面/pre_chapter");
            }else if(Integer.parseInt(page_id)<8){
                toolkit.leftClickArea("章节界面/next_chapter");
            }
            toolkit.sleep(3);
            page_id=toolkit.extractText("章节界面/章节编号").replace("第",
                    "").replace("章","").trim();
        }
        toolkit.sleep(3);
        toolkit.leftClickArea("章节界面/hardmode");
//        Toolkit toolkit =new Toolkit(connector,"./");
//        toolkit.setNowDir("test");
//        toolkit.rightClickArea("main/entry");
    }
}
