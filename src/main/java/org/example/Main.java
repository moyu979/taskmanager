package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Component.Servers;
import org.example.Cmd.Cmd;

import java.util.Scanner;

import static java.lang.Thread.sleep;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    static Servers server;

    public static void main(String[] args) throws Exception {
        logger.info("start main thread");
        server = new Servers();
        Thread thread=new Thread(server);
        logger.info("server running");
        thread.start();
        logger.info("start cmd thread");
        Cmd cmd=new Cmd(server);
        cmd.run();

    }

}