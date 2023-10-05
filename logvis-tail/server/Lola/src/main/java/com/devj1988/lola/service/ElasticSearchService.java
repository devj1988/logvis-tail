package com.devj1988.lola.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.IndicesStatsResponse;
import com.devj1988.lola.config.ESConfig;
import com.devj1988.lola.model.SyslogMessage;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ElasticSearchService {
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String INDEX_PREFIX = "logs-";
    private static final int MAX_RESULTSET_SIZE = 3000;

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticSearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void indexMessage(SyslogMessage message) {
        try {
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index(getIndexForApplication(message.getApplication()))
                    .document(message)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<String>> getLogs(String application, String fromDateTime, String toDateTime) throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(getIndexForApplication(application))
                .query(QueryBuilders.bool(q -> q.filter(
                        f -> f.range(
                                r -> r.from(fromDateTime)
                                        .to(toDateTime)
                                        .field("timestamp")
                ))))
                .size(MAX_RESULTSET_SIZE)
                .sort(s -> s.field(f -> f.field("timestamp").order(SortOrder.Asc)))
                .build();
        SearchResponse<ObjectNode> response = elasticsearchClient.search(searchRequest, ObjectNode.class);

        List<List<String>> logs = response.hits().hits()
                .stream()
                .map(h -> {
                        List<String> l = new ArrayList(2);
                        l.add(h.source().get("source").asText());
                        l.add(h.source().get("log").asText());
                        return l;
                    })
                .collect(Collectors.toList());

        return logs;
    }

    public Set<String> getApplicationList() {
        IndicesStatsResponse stats = null;
        try {
            stats = elasticsearchClient.indices().stats();
        } catch (IOException e) {
            return Collections.emptySet();
        }

        return stats.indices().keySet().stream()
                .filter(x -> x.startsWith(INDEX_PREFIX))
                .map(x -> x.substring(INDEX_PREFIX.length()))
                .collect(Collectors.toSet());
    }

    public boolean applicationExists(String application) {
        return getApplicationList().contains(application);
    }

    private String getIndexForApplication(String application) {
        return INDEX_PREFIX + application;
    }
}
