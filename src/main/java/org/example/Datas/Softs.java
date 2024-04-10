package org.example.Datas;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.example.Component.Machine;
import org.example.Component.Servers;
import org.example.Component.Software;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
class empty implements Software{
    Machine machine;
    File dataFile=null;
    public empty(Machine machine){
        this.machine=machine;
    }

    @Override
    public Calendar run() {
        System.out.println("empty running "+machine.name+"@"+machine.port);
        Calendar now=Calendar.getInstance();
        now.add(Calendar.SECOND,10);
        return now;
    }

    @Override
    public void setDatas(File dataFile) {
        if(this.dataFile==null){
            this.dataFile=dataFile;
        }
    }

    @Override
    public String getInfo() {
        return null;
    }
}
public class Softs {
    private static final Logger logger = LogManager.getLogger(Softs.class);

    public static Map<String, Class<?>> clazzes =new HashMap<>();
    public static Map<String, File> machineFiles=new HashMap<>();

    static {
        clazzes.put("empty",empty.class);
        machineFiles.put("empty",new File("this_is_null_file"));
    }
    public static void loadClass(File file){
        try{
            //载入该软件对应的类
            String name=file.getName().replace(".jar","");
            URL url=file.toURI().toURL();
            URLClassLoader urlClassLoader=new URLClassLoader(new URL[]{url},Thread.currentThread().getContextClassLoader());
            Class<?> myClass=urlClassLoader.loadClass(name);
            clazzes.put(name,myClass);
            //生成存储软件设置的文件夹
            File softwares=new File(Servers.workFile,"softwares");
            File softFile=new File(softwares,name);
            machineFiles.put(name,softFile);

        } catch (MalformedURLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void loadAllClass(File file){
        File[] files=file.listFiles();
        assert files != null;
        for(File f:files){
            if(f.getName().endsWith(".jar")){
                loadClass(f);
            }
        }
    }
    public static Software getInstance(String name,Machine machine){
        try {
            Software software=(Software) clazzes.get(name).getDeclaredConstructor(Machine.class).newInstance(machine);
            software.setDatas(machineFiles.get(name));//传入存储类唯一运行时数据的东西
            return software;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Software> getAllInstance(Machine machine){
        ArrayList<Software> softList=new ArrayList<>();
        for(Map.Entry<String,Class<?>> entry: clazzes.entrySet()){
            //获得实例
            Software f=getInstance(entry.getKey(),machine);
            softList.add(f);
        }
        return softList;
    }
}
