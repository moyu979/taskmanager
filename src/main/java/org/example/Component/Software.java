package org.example.Component;

import java.io.File;
import java.util.Calendar;

public interface Software {

    Calendar run();
    void setDatas(File data_path);
    String getInfo();
}
