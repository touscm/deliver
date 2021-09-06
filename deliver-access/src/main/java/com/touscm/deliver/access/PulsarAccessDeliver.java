package com.touscm.deliver.access;

import com.touscm.deliver.pulsar.autoconfigure.PulsarProperties;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;

@Service("pulsarAccessDeliver")
public class PulsarAccessDeliver implements IAccessDeliver {
    private static final Logger logger = LoggerFactory.getLogger(PulsarAccessDeliver.class);

    @Resource
    private PulsarProperties config;
    @Resource
    private PulsarClient client;

    private static final Object locker = new Object();
    private Producer<AccessEntry> producer;

    @Override
    public boolean process(AccessEntry accessEntry) {
        if (accessEntry == null) return false;

        setProducer();

        try {
            logger.debug("发送请求记录消息, entry:{}", StringUtils.toJson(accessEntry));
            return producer.send(accessEntry) != null;
        } catch (PulsarClientException e) {
            logger.error("发送请求记录消息异常, entry:{}", StringUtils.toJson(accessEntry), e);
        }

        return false;
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
        if (producer != null) {
            producer.close();
        }
    }

    /* ...... */

    private void setProducer() {
        synchronized (locker) {
            if (producer == null) {
                String topic = config.getAccessTopic();
                if (topic == null || topic.isEmpty()) {
                    throw new RuntimeException("未配置Pulsar请求记录Topic");
                }

                String producerName = StringUtils.isBlank(config.getAccessProducer()) ? PRODUCER : config.getAccessProducer();
                Schema<AccessEntry> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(AccessEntry.class).build());

                try {
                    producer = client.newProducer(schema).topic(topic).producerName(producerName).create();
                } catch (PulsarClientException e) {
                    logger.error("创建Producer异常, topic:{}, producer:{}", topic, producerName, e);
                    throw new RuntimeException("创建Producer异常", e);
                }
            }
        }
    }
}
