package org.example.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Datas.Softs;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Thread.sleep;

public class Machine implements Runnable{
    private final Logger logger;
    public String port;
    public String name;
    public File workFile;
    ArrayList<Software> softList;
    int runCount=0;

    Calendar nextRun;
    Machine(String name,String port){
        this.port= port;
        this.name=name;
        nextRun=Calendar.getInstance();
        this.workFile=new File("");//todo design it
        logger = LogManager.getLogger(Servers.class+"("+name+"@"+port+")");
        softList= Softs.getAllInstance(this);
        logger.info("init finished");

    }

    @Override
    public void run() {
        if(nextRun.before(Calendar.getInstance())){
            nextRun.add(Calendar.HOUR,100);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("running "+ runCount +"times");
            for(Software software:softList){
                Calendar temp=software.run();
                Calendar now=Calendar.getInstance();
                now.add(Calendar.HOUR,1);
                if(temp.before(nextRun) && temp.after(now)){
                    nextRun=temp;
                }
            }
        }

    }
    public Calendar getNextRun(){
        return nextRun;
    }
    public void getScreen(){
        //todo getScreen
    }
}