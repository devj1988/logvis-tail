package com.devj1988.lola.kafka;

import com.devj1988.lola.model.SyslogMessage;
import com.devj1988.lola.service.ElasticSearchService;
import com.devj1988.lola.service.SSEService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;

@Component
@Slf4j
public class SyslogListener {
    private final ElasticSearchService elasticSearchService;
    private final SSEService sseService;

    public SyslogListener(ElasticSearchService elasticSearchService,
                          SSEService sseService) {
        this.elasticSearchService = elasticSearchService;
        this.sseService = sseService;
    }

    @KafkaListener(topics = "${kafka.consumer.topic}", groupId = "${kafka.consumer.groupId}", containerFactory = "kafkaListenerContainerFactory")
    public void listenForSyslogMessages(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        log.info("Received Message: " + message + " from partition: " + partition);
        SyslogMessage syslogMessage = parse(message);

        elasticSearchService.indexMessage(syslogMessage);
        sseService.newSysLogMessage(syslogMessage);
    }

    private SyslogMessage parse(String message) {
        String preMessage =  message.substring(0, message.indexOf("]:") + 2);
        String log = message.substring(message.indexOf("]:") + 2).trim();
        String[] parts = preMessage.split(" ");
        Instant ts = Instant.parse(parts[0]);
        String host = parts[1];
        String source = parts[2].substring(0, parts[2].indexOf("["));
        String application = parts[2].split("-")[0];

        SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.setApplication(application);
        SimpleDateFormat outputFormat = new SimpleDateFormat(ElasticSearchService.TIMESTAMP_FORMAT);
        syslogMessage.setTimestamp(outputFormat.format(Date.from(ts)));
        syslogMessage.setSource(source);
        syslogMessage.setHost(host);
        syslogMessage.setLog(log);

        return syslogMessage;
    }

//    public static void main(String[] args) {
//        SyslogListener listener = new SyslogListener(null);
//        String message = "2023-10-03T00:33:33+00:00 172.30.0.1 hellosb-aefa7a65f31a[1408]: { \"abc\": 123 }";
//        listener.parse(message);
//    }
}
