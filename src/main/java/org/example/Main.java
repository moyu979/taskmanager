package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main {
    private static Logger logger = LogManager.getLogger("liuyxlogger");
    private static Logger logger2 = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        logger.trace("hello liuyx log");
        logger.info("hello");
        logger.error("im an error");
        logger2.info("from logger2");
        logger2.error("im an error");
    }
}