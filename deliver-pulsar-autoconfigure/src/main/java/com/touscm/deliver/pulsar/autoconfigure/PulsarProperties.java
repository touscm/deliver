package com.touscm.deliver.pulsar.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = PulsarProperties.PREFIX)
public class PulsarProperties {
    public static final String PREFIX = "deliver.pulsar";

    public static final int DEFAULT_PORT = 6650;
    public static final String DEFAULT_SCHEME = "pulsar://%s:%d";

    /**
     * pulsar://localhost:6650
     */
    private String url;
    /**
     * localhost
     */
    private String host;
    /**
     * 6650
     */
    private int port;

    /**
     * access
     */
    private String accessTopic;
    /**
     * access-subscribe
     */
    private String accessSubscribe;

    /**
     * log
     */
    private String logTopic;
    /**
     * log-subscribe
     */
    private String logSubscribe;

    public String getUrl() {
        if (url == null || url.isEmpty()) {
            return String.format(DEFAULT_SCHEME, host, port <= 0 ? DEFAULT_PORT : port);
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getAccessTopic() {
        return accessTopic;
    }

    public void setAccessTopic(String accessTopic) {
        this.accessTopic = accessTopic;
    }

    public String getAccessSubscribe() {
        return accessSubscribe;
    }

    public void setAccessSubscribe(String accessSubscribe) {
        this.accessSubscribe = accessSubscribe;
    }

    public String getLogTopic() {
        return logTopic;
    }

    public void setLogTopic(String logTopic) {
        this.logTopic = logTopic;
    }

    public String getLogSubscribe() {
        return logSubscribe;
    }

    public void setLogSubscribe(String logSubscribe) {
        this.logSubscribe = logSubscribe;
    }
}
