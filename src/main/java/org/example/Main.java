package org.example;

import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Component.Servers;

import static java.lang.Thread.sleep;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);


    public static void main(String[] args) throws InterruptedException {
        Servers server= null;
        try {
            server = new Servers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Thread thread=new Thread(server);
        thread.start();
        sleep(20000);
        thread.interrupt();
        logger.debug("quit main thread,please wait for other thread finished");
    }
}