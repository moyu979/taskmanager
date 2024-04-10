package org.example.Cmd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Component.Servers;
import org.example.Main;

import java.util.Scanner;

public class Cmd {
    private static final Logger logger = LogManager.getLogger(Cmd.class);
    Servers server;
    public Cmd(Servers servers){
        this.server=servers;
    }
    public void run(){

        Scanner scanner=new Scanner(System.in);
        boolean quit=false;
        while(!quit){
            String cmd=scanner.nextLine();
            quit=analyze(cmd);
        }
        logger.debug("quit main thread,please wait for other thread finished");
    }

    boolean analyze(String cmd){
        String[] paras=cmd.split(" ");
        if(paras[0]=="quit"){
            return true;
        }else{
            switch (paras[0]){
                case "addMachine"->server.addMachine(paras[0],paras[1]);
                case "info"->server.getInfo();
            }
        }
        return false;
    }
}
