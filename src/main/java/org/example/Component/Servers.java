package org.example.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Datas.Softs;
import org.example.Tools.CreateWorkTree;


import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Servers implements Runnable{
    private static final Logger logger = LogManager.getLogger(Servers.class);

    //name,machines
    Map<String,Machine> machines;
    public static File workFile;
    Calendar nextRun;

    public Servers() throws Exception {
        workFile =new File("settings");
        constructor();
    }

    public Servers(String workPath) throws Exception {
        workFile =new File(workPath);
        constructor();
    }

    private void constructor() throws Exception {
        //生成工作树
        CreateWorkTree.createworkTree(workFile);

        Softs.loadAllClass(new File(workFile,"softwares"));

        machines= new HashMap<>();
        logger.info("working in "+workFile.getAbsolutePath());

        logger.debug("start load settings");
        File settingFile=new File(workFile,"settings");
        FileInputStream fileInputStream=new FileInputStream(settingFile);
        InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
        String aLine=bufferedReader.readLine();
        while(aLine!=null){
            logger.info("init with "+aLine);
            String[] lines=aLine.split(";");
            Machine machine=new Machine(lines[0],lines[1]);
            machines.put(machine.name,machine);
            logger.info("init machine with name "+lines[0]+" and port "+lines[1]+" success");
            aLine=bufferedReader.readLine();
        }
        logger.debug("constructor end "+machines.size());

        nextRun=Calendar.getInstance();
    }

    public void run(){
        logger.debug("runnn");
        while(!Thread.currentThread().isInterrupted() && Calendar.getInstance().after(nextRun)){
            ArrayList<Thread> threads=new ArrayList<Thread>();
            logger.debug("running");
            //新建一组线程
            for(Map.Entry<String,Machine> entry:machines.entrySet()){
                Thread thread=new Thread(entry.getValue());
                thread.start();
                threads.add(thread);
            }
            //运行等待
            for(Thread thread:threads){
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
            logger.debug("finished running");
            //统计下一次运行
            nextRun=Calendar.getInstance();
            nextRun.add(Calendar.YEAR,1);
            for(Map.Entry<String,Machine> entry:machines.entrySet()){
                if(nextRun.after(entry.getValue().nextRun)){
                    nextRun=entry.getValue().nextRun;
                }
            }
            logger.info("next run "+ nextRun.toString());

            //运行完停一会儿
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void getInfo(){
        //TODO 获得机器信息
    }

    public void addMachine(String para, String para1) {
        //Todo 增加一台机器
    }

    public void add_soft(String softPath){
        //Todo 增加一个软件
    }
    public void del_machine(String port,String name){
        //Todo 删除一个机器
    }
    public void del_soft(String softPath){
        //Todo 删除一台机器
    }
    public void close_server(){
        //Todo 停止服务
    }



}