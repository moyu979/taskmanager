package org.example.Component;

public class Machine implements Runnable{
    String port;
    String paraPath;

    Machine(String port,String paraPath){
        this.port= port;
        this.paraPath=paraPath;
    }
    @Override
    public void run() {

    }
}