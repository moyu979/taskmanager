package org.example.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Main;

import java.io.File;
import java.io.IOException;

public class CreateWorkTree {
    private static final Logger logger = LogManager.getLogger(CreateWorkTree.class);
    public static void createworkTree(File path) throws IOException {
        if(!path.exists()){
            // ./
            logger.info("creating a new work space");
            path.mkdir();
            // ./settings
            File settings=new File(path,"settings");
            if(!settings.exists()){
                settings.createNewFile();
            }
            // ./software
            File sofwares=new File(path,"softwares");
            if (!sofwares.exists()){
                sofwares.mkdir();
            }
            // ./userFile
            File userFile=new File(path,"userFile");
            if (!userFile.exists()){
                userFile.mkdir();
            }
        }
    }
}
