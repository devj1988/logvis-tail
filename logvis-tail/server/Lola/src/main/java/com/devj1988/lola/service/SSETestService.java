package com.devj1988.lola.service;

import com.devj1988.lola.model.SyslogMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SSETestService {
    private final SSEService sseService;

    @Autowired
    public SSETestService(SSEService sseService) {
        this.sseService = sseService;
    }


//    @Scheduled(fixedDelay = 2000)
    public void scheduleFixedDelayTask() {
        SyslogMessage message = new SyslogMessage();
        message.setLog("new log " + Instant.now().toString());
        message.setSource("source1");
        message.setApplication("hellosb");

        sseService.newSysLogMessage(message);
    }
}
