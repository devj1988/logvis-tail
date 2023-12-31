package com.devj1988.lola.controller;

import com.devj1988.lola.service.ElasticSearchService;
import com.devj1988.lola.service.SSEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class LogsController {

    private final ElasticSearchService elasticSearchService;
    private final SSEService sseService;

    @Autowired
    public LogsController(ElasticSearchService elasticSearchService,
                          SSEService sseService) {
        this.elasticSearchService = elasticSearchService;
        this.sseService = sseService;
    }

    @GetMapping("/list-apps")
    public ResponseEntity<List<String>> getApps() {
        return ResponseEntity.of(Optional.of(elasticSearchService.getApplicationList().stream().toList()));
    }

    @GetMapping("/logs/{application}")
    public ResponseEntity<List<List<String>>> getLogs(@PathVariable String application,
                                                @RequestParam Optional<String> start,
                                                @RequestParam Optional<String> end) {

        if (!elasticSearchService.applicationExists(application)) {
            return ResponseEntity.badRequest().build();
        }
        List<List<String>> ret;
        SimpleDateFormat dateFormat = new SimpleDateFormat(ElasticSearchService.TIMESTAMP_FORMAT);
        if (start.isEmpty() && end.isEmpty()) {
            String startTime = dateFormat.format(Date.from(Instant.now().minusSeconds(5)));
            String endTime = dateFormat.format(Date.from(Instant.now()));

            try {
                ret = elasticSearchService.getLogs(application, startTime, endTime);
            } catch (IOException e) {
                return ResponseEntity.internalServerError()
                        .build();
            }
        } else if (start.isPresent() && end.isPresent()) {
            try {
                dateFormat.parse(start.get());
                dateFormat.parse(end.get());

                try {
                    ret = elasticSearchService.getLogs(application, start.get(), end.get());
                } catch (IOException e) {
                    return ResponseEntity.internalServerError()
                            .build();
                }
            } catch (ParseException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.of(Optional.ofNullable(ret));
    }

    @GetMapping("/tail-logs/{application}")
    public SseEmitter streamSseMvc(@PathVariable String application) {
        return sseService.getEmitterForApplication(application);
    }
}
