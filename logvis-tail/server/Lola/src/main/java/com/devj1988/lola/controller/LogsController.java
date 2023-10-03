package com.devj1988.lola.controller;

import com.devj1988.lola.es.ElasticSearchService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class LogsController {

    private final ElasticSearchService elasticSearchService;

    @Autowired
    public LogsController(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @GetMapping("/logs/{application}")
    public ResponseEntity<List<List<String>>> getLogs(@PathVariable String application,
                                                @RequestParam Optional<String> start,
                                                @RequestParam Optional<String> end) {
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
}
