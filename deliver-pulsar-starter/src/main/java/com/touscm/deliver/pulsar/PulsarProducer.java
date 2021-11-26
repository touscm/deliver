package com.touscm.deliver.pulsar;

import com.touscm.deliver.base.utils.EntryUtils;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Scope("prototype")
@Service("PulsarProducer")
public class PulsarProducer<T> implements IProducer<T> {
    private static final Logger logger = LoggerFactory.getLogger(PulsarProducer.class);

    @Resource
    private PulsarClient client;

    private Producer<T> producer;

    /**
     * initialize message entry type and set topic
     *
     * @param entryType    message entry type
     * @param topic        topic
     * @param producerName producer name
     */
    public void init(@NotNull Class<T> entryType, @NotBlank String topic, String producerName) {
        Assert.notNull(entryType, "Entry type can't be null");
        Assert.hasText(topic, "Topic can't be empty");

        initProducer(entryType, topic, producerName);
    }

    /**
     * send message
     *
     * @param entry message entry
     * @return send result state
     */
    public boolean send(@NotNull T entry) {
        Assert.notNull(entry, "Send entry can't be null");

        if (producer == null) throw new RuntimeException("Please call init() method first");

        try {
            MessageId messageId = producer.send(entry);
            logger.debug("send message success, topic:{}, producerName:{}, messageId:{}, entry:{}", producer.getTopic(), producer.getProducerName(), messageId, EntryUtils.toString(entry));
            return true;
        } catch (PulsarClientException e) {
            logger.error("send message with exception, topic:{}, producerName:{}, entry:{}", producer.getTopic(), producer.getProducerName(), EntryUtils.toString(entry), e);
        }
        return false;
    }

    /**
     * send message after the specified relative delay<br>
     * <b>Note</b>: messages are only delivered with delay when a consumer is consuming through a SubscriptionType.Shared subscription. With other subscription types, the messages will still be delivered immediately.
     *
     * @param entry message entry
     * @param delay the amount of delay before the message will be delivered
     * @param unit  the time unit for the delay
     * @return send result state
     */
    public boolean sendAfter(@NotNull T entry, long delay, TimeUnit unit) {
        Assert.notNull(entry, "Send entry can't be null");

        if (producer == null) throw new RuntimeException("Please call init() method first");

        try {
            MessageId messageId = producer.newMessage().value(entry).deliverAfter(delay, unit).send();
            logger.debug("send message success, topic:{}, producerName:{}, messageId:{}, entry:{}", producer.getTopic(), producer.getProducerName(), messageId, EntryUtils.toString(entry));
            return true;
        } catch (PulsarClientException e) {
            logger.error("send message with exception, topic:{}, producerName:{}, entry:{}", producer.getTopic(), producer.getProducerName(), EntryUtils.toString(entry), e);
        }
        return false;
    }

    /**
     * send message at the specified timestamp<br>
     * <b>Note</b>: messages are only delivered with delay when a consumer is consuming through a SubscriptionType.Shared subscription. With other subscription types, the messages will still be delivered immediately.
     *
     * @param entry     message entry
     * @param timestamp timestamp is milliseconds and based on UTC
     * @return send result state
     */
    public boolean sendAt(@NotNull T entry, long timestamp) {
        Assert.notNull(entry, "Send entry can't be null");

        if (producer == null) throw new RuntimeException("Please call init() method first");

        try {
            MessageId messageId = producer.newMessage().value(entry).deliverAt(timestamp).send();
            logger.debug("send message success, topic:{}, producerName:{}, messageId:{}, entry:{}", producer.getTopic(), producer.getProducerName(), messageId, EntryUtils.toString(entry));
            return true;
        } catch (PulsarClientException e) {
            logger.error("send message with exception, topic:{}, producerName:{}, entry:{}", producer.getTopic(), producer.getProducerName(), EntryUtils.toString(entry), e);
        }
        return false;
    }

    /* ...... */

    /**
     * close pulsar producer
     *
     * @throws IOException PulsarClientException
     */
    public void close() throws IOException {
        if (producer != null) producer.close();
    }

    /* ...... */

    private void initProducer(Class<T> entryType, String topic, String producerName) {
        if (producer != null) {
            logger.warn("producer has initialize, entryType:{}, topic:{}, producerName:{}", entryType.getName(), topic, producerName);
            return;
        }

        try {
            Schema<T> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(entryType).build());
            ProducerBuilder<T> builder = client.newProducer(schema).topic(topic);

            if (producerName != null && !producerName.isEmpty()) {
                builder.producerName(producerName);
            }

            producer = builder.create();
        } catch (PulsarClientException e) {
            logger.error("create producer exception, entryType:{}, topic:{}, producerName:{}", entryType.getName(), topic, producerName, e);
            throw new RuntimeException("create producer exception", e);
        }
    }
}
