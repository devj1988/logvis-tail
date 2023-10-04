package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class LogEmitter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final Logger logger = LoggerFactory.getLogger(LogEmitter.class);

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        logger.info("The time is now {}", dateFormat.format(new Date()));
    }
}
