package com.devj1988.lola.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class ESConfig {
    @Bean
    public ElasticsearchClient elasticsearchClient(@Value(value = "${elasticsearch.host}") String esHost,
                                                   @Value(value ="${elasticsearch.port}") int esPort) {
        RestClient restClient = RestClient.builder(
                new HttpHost(esHost, esPort))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Content-type", "application/json")
                })
                .setHttpClientConfigCallback(hc -> hc
                        .addInterceptorLast( (HttpResponseInterceptor)
                                (response, context) ->
                                        response.addHeader("X-Elastic-Product", "Elasticsearch"))

                )
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }
}
