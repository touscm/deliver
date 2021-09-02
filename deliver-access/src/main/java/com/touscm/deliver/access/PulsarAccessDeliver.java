package com.touscm.deliver.access;

import com.google.gson.Gson;
import com.touscm.deliver.pulsar.autoconfigure.PulsarProperties;
import org.apache.pulsar.client.api.*;
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

    private static final Gson gson = new Gson();

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

        logger.debug("投递请求记录, entry:{}", gson.toJson(accessEntry));

        try {
            return producer.send(accessEntry) != null;
        } catch (PulsarClientException e) {
            logger.error("投递请求记录异常, entry:{}", gson.toJson(accessEntry), e);
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

                Schema<AccessEntry> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(AccessEntry.class).build());

                try {
                    producer = client.newProducer(schema).topic(topic).create();
                } catch (PulsarClientException e) {
                    logger.error("创建Producer异常, topic:{}", topic, e);
                    throw new RuntimeException("创建Producer异常", e);
                }
            }
        }
    }
}
