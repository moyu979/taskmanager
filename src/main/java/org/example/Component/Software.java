package org.example.Component;

import java.io.File;
import java.util.Calendar;

public interface Software {

    void run();
    Calendar getNextRunTime();
    void setSettings(File settingsFile,File workFile);
}
