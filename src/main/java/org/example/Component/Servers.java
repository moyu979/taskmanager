package org.example.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Servers implements Runnable{
    private static final Logger logger = LogManager.getLogger(Servers.class);

    //name,machines
    Map<String,Machine> machines;
    File workFile;
    public Servers() throws Exception {
        workFile =new File("settings");
        constructor();
    }

    public Servers(String workPath) throws Exception {
        workFile =new File(workPath);
        constructor();
    }

    /*private void loadSofts() {
        File softFile=new File(workFile,"softs");
        if(!workFile.exists()){
            logger.info("create new softFile, no soft will be run");
            boolean success=workFile.mkdir();
            if(!success){
                logger.error("softFile in "+softFile.getPath()+" create failed");
                return;
            }
        }
        File[] lists=workFile.listFiles();
        assert lists != null;
        for(File file:lists){
            if(file.getName().endsWith(".jar")){
                if(loadSoft(file)){
                    logger.info("load "+file.getName());
                }else{
                    logger.warn("load "+file.getName()+" failed");
                }
            }
        }
    }*/



    private void constructor() throws Exception {
        machines= new HashMap<>();
        logger.debug("constructor start with path"+workFile.getPath());
        logger.info("working in "+workFile.getAbsolutePath());

        if(!workFile.exists()){
            File defaultSetting=new File("src/main/resources/settings");
            if(defaultSetting.exists()){
                logger.info("start with default settings");
                copySettings(defaultSetting);
            }else{
                logger.error("init settings error, existing");
            }
        }
        logger.debug("start load settings");
        File settingFile=new File(workFile,"settings");
        FileInputStream fileInputStream=new FileInputStream(settingFile);
        InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
        String aLine=bufferedReader.readLine();
        while(aLine!=null){
            String[] lines=aLine.split(";");
            Machine machine=new Machine(lines[0],lines[1]);
            if(machine!=null){
                logger.error("init machine with name"+lines[0]+" and port "+lines[1]+" failed");
            }else{
                machines.put(machine.name,machine);
                logger.info("init machine with name "+lines[0]+" and port "+lines[1]+" success");
            }
            aLine=bufferedReader.readLine();
        }
        logger.debug("constructor end");
        Machine machine1=new Machine("1234","1");
        Machine machine2=new Machine("1234","2");
        machines.put("1",machine1);
        machines.put("2",machine2);
    }

    private void copySettings(File defaultSetting) {
        logger.info("copping default files");
        try {
            Files.copy(Path.of(defaultSetting.getPath()), Path.of(workFile.getPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            ArrayList<Thread> threads=new ArrayList<Thread>();
            logger.debug("running");
            for(Map.Entry<String,Machine> entry:machines.entrySet()){
                Thread thread=new Thread(entry.getValue());
                thread.start();
                threads.add(thread);
            }
            for(Thread thread:threads){
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
            logger.debug("finished running");
        }

    }

    void getInfo(){
        //TODO 获得机器信息
    }

}