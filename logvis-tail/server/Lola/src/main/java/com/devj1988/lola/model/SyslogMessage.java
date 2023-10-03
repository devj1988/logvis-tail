package com.devj1988.lola.model;

import co.elastic.clients.util.DateTime;
import lombok.Data;

import java.time.Instant;

@Data
public class SyslogMessage {
    private String timestamp;
    private String host;
    private String source;
    private String application;
    private String log;
}
