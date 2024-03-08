package org.example.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Datas.Softs;

import java.io.File;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Machine implements Runnable{
    private Logger logger;
    String port;
    String name;
    File workFile;
    ArrayList<Software> softList;
    int runCount=0;

    Machine(String port,String name){
        this.port= port;
        this.name=name;
        this.workFile=new File("");//todo design it
        logger = LogManager.getLogger(Servers.class+"("+name+"@"+port+")");
        softList= Softs.getAllInstance(workFile);
        logger.info("init finished");
    }

    @Override
    public void run() {
        logger.info("running "+ runCount +"times");
        //runCount++;
        try {
            sleep(5000);
        } catch (InterruptedException e) {

            throw new RuntimeException(e);
        }
    }
}