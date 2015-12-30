package com.gmi.nordborglab.browser.server.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by uemit.seren on 12/29/15.
 */
@Configuration
@PropertySource(value = "file:///${ext.prop.dir}es_${spring.profiles.active}.properties")
public class ElasticsearchConfig {

    @Autowired
    Environment env;

    @Bean
    public Client esClient() throws UnknownHostException {
        return TransportClient.builder().settings(settings())
                .addPlugin(DeleteByQueryPlugin.class)
                .build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(env.getProperty("ELASTICSEARCH.host")), env.getProperty("ELASTICSEARCH.port", Integer.class, 9300)));

    }

    public Settings settings() {
        return Settings.settingsBuilder()
                .put("cluster.name", env.getProperty("cluster.name"))
                .build();
    }


}
