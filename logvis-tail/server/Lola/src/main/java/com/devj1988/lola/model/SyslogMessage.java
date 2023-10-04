package com.devj1988.lola.model;

import lombok.Data;

@Data
public class SyslogMessage {
    private String timestamp;
    private String host;
    private String source;
    private String application;
    private String log;
}
