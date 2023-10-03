package com.devj1988.lola.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.devj1988.lola.config.ESConfig;
import com.devj1988.lola.model.SyslogMessage;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ElasticSearchService {
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

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
            log.info("Indexed with version " + response.version());
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
                )))).build();
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

    private String getIndexForApplication(String application) {
        return "logs-" + application;
    }

    public static void main(String[] args) throws IOException {
        ESConfig config = new ESConfig();
        ElasticsearchClient esclient = config.elasticsearchClient("localhost", 9200);
        ElasticSearchService service = new ElasticSearchService(esclient);
        service.getLogs("hellosb", "2023-10-03T15:54:05.000+0000",
                "2023-10-03T15:59:55.000+0000");
    }
}
