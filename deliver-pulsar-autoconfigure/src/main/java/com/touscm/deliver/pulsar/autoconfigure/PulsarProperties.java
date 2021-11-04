package com.touscm.deliver.pulsar.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = PulsarProperties.PREFIX)
public class PulsarProperties {
    public static final String PREFIX = "deliver.pulsar";

    public static final int DEFAULT_PORT = 6650;
    public static final String DEFAULT_URL = "pulsar://%s:%d";
    public static final String UDF_SCHEME_URL = "%s://%s:%d";

    /**
     * pulsar://localhost:6650
     */
    private String url;

    /**
     * connect scheme, default is pulsar
     */
    private String scheme;
    /**
     * localhost
     */
    private String host;
    /**
     * 6650
     */
    private int port;
    /**
     * auth token
     */
    private String token;

    /**
     * access-topic
     */
    private String accessTopic;
    /**
     * access-producer
     */
    private String accessProducer;
    /**
     * access-subscribe
     */
    private String accessSubscribe;
    /**
     * access-executor-count
     */
    private int accessExecutorCount;

    /**
     * log
     */
    private String logTopic;
    /**
     * log-producer
     */
    private String logProducer;
    /**
     * log-subscribe
     */
    private String logSubscribe;
    /**
     * log-executor-count
     */
    private int logExecutorCount;

    public String getUrl() {
        if (url == null || url.isEmpty()) {
            if (scheme == null || scheme.isEmpty()) {
                return String.format(DEFAULT_URL, host, port <= 0 ? DEFAULT_PORT : port);
            } else {
                return String.format(UDF_SCHEME_URL, scheme, host, port <= 0 ? DEFAULT_PORT : port);
            }
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccessTopic() {
        return accessTopic;
    }

    public void setAccessTopic(String accessTopic) {
        this.accessTopic = accessTopic;
    }

    public String getAccessProducer() {
        return accessProducer;
    }

    public void setAccessProducer(String accessProducer) {
        this.accessProducer = accessProducer;
    }

    public String getAccessSubscribe() {
        return accessSubscribe;
    }

    public void setAccessSubscribe(String accessSubscribe) {
        this.accessSubscribe = accessSubscribe;
    }

    public int getAccessExecutorCount() {
        return accessExecutorCount;
    }

    public void setAccessExecutorCount(int accessExecutorCount) {
        this.accessExecutorCount = accessExecutorCount;
    }

    public String getLogTopic() {
        return logTopic;
    }

    public void setLogTopic(String logTopic) {
        this.logTopic = logTopic;
    }

    public String getLogProducer() {
        return logProducer;
    }

    public void setLogProducer(String logProducer) {
        this.logProducer = logProducer;
    }

    public String getLogSubscribe() {
        return logSubscribe;
    }

    public void setLogSubscribe(String logSubscribe) {
        this.logSubscribe = logSubscribe;
    }

    public int getLogExecutorCount() {
        return logExecutorCount;
    }

    public void setLogExecutorCount(int logExecutorCount) {
        this.logExecutorCount = logExecutorCount;
    }
}
