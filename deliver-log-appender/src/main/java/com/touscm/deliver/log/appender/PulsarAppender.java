package com.touscm.deliver.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.touscm.deliver.log.LogEntry;
import com.touscm.deliver.log.StringUtils;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;

public class PulsarAppender extends AppenderBase<ILoggingEvent> implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PulsarAppender.class);

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 6650;
    public static final String PRODUCER = "log-producer";

    private PulsarClient client;
    private Producer<LogEntry> producer;

    /* ...... */

    /**
     * pulsar://127.0.0.1:6650
     */
    private String url;
    /**
     * 127.0.0.1
     */
    private String host;
    /**
     * 6650
     */
    private int port;
    /**
     * log-topic
     */
    private String topic;
    /**
     * log-producer
     */
    private String producerName;

    public String getUrl() {
        return url;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    /* ...... */

    @Override
    public void start() {
        initPulsarClient();
        initPulsarProducer();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        LogEntry entry = LogEntry.parse(event);
        try {
            producer.send(entry);
        } catch (PulsarClientException e) {
            logger.error("发送日志消息异常, event:{}, log:{}", StringUtils.toJson(event), e);
        }
    }

    @Override
    public void stop() {
        super.stop();

        if (producer != null) {
            try {
                producer.close();
            } catch (PulsarClientException e) {
                logger.error("close Producer with exception", e);
            }
            producer = null;
        }
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        if (producer != null) {
            try {
                producer.close();
            } catch (PulsarClientException e) {
                logger.error("close Producer with exception", e);
            }
        }
        if (client != null) {
            try {
                client.close();
            } catch (PulsarClientException e) {
                logger.error("close PulsarClient with exception", e);
            }
        }
    }

    /* ...... */

    private void initPulsarClient() {
        url = String.format("pulsar://%s:%d", StringUtils.isEmpty(host) ? HOST : host, port < 80 ? PORT : port);
        try {
            client = PulsarClient.builder().serviceUrl(url).build();
        } catch (PulsarClientException e) {
            logger.error("创建日志PulsarClient异常, url:{}", url, e);
            throw new RuntimeException("创建日志PulsarClient异常", e);
        }
    }

    private void initPulsarProducer() {
        if (producer == null) {
            if (StringUtils.isEmpty(topic)) throw new RuntimeException("未指定Pulsar日志Topic");

            String producerName = StringUtils.isBlank(this.producerName) ? PRODUCER : this.producerName;
            Schema<LogEntry> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(LogEntry.class).build());

            try {
                producer = client.newProducer(schema).topic(topic).producerName(producerName).create();
            } catch (PulsarClientException e) {
                logger.error("创建日志Producer异常, topic:{}, producer:{}", topic, producerName, e);
                throw new RuntimeException("创建日志Producer异常", e);
            }
        }
    }
}
