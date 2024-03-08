package org.example.Datas;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.example.Component.Software;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Softs {
    private static final Logger logger = LogManager.getLogger(Softs.class);

    public static Map<String, Class<?>> m=new HashMap<>();
    public static void addClass(String name, Class<?> clazz){
        m.put(name,clazz);
    }
    public static Class<?> getClass(String name){
        return m.get(name);
    }

    public static Map<String,Class<?>> getAll(){
        return m;
    }
    public static void loadClass(String s){
        File f=new File(s);
    }
    public static void loadClass(File file){
        try{
            String name=file.getName().replace(".jar","");
            URL url=file.toURI().toURL();
            URLClassLoader urlClassLoader=new URLClassLoader(new URL[]{url},Thread.currentThread().getContextClassLoader());
            Class<?> myClass=urlClassLoader.loadClass(name);
            m.put(name,myClass);
        } catch (MalformedURLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void loadAllClass(File file){
        File[] files=file.listFiles();
        for(File f:files){
            if(f.getName().endsWith(".jar")){
                loadClass(f);
            }
        }
    }
    public static Software getInstance(String name,File settingsFile,File workFile){
        try {
            Software f=(Software) m.get(name).getDeclaredConstructor().newInstance();
            f.setSettings(settingsFile,workFile);
            return f;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Software> getAllInstance(File upperFile){
        ArrayList<Software> softList=new ArrayList<>();
        for(Map.Entry<String,Class<?>> entry: m.entrySet()){
            try {
                Software f=(Software) entry.getValue().getDeclaredConstructor().newInstance();
                f.setSettings(new File(upperFile,"settings"),new File(upperFile,entry.getKey()));
                softList.add(f);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return softList;
    }
}
