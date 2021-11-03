package com.touscm.deliver.pulsar;

import com.touscm.deliver.base.utils.EntryUtils;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

@Service("PulsarProducer")
public class PulsarProducer<T> implements IProducer<T> {
    private static final Logger logger = LoggerFactory.getLogger(PulsarProducer.class);

    private boolean isInitProducer = false;

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

        if (!isInitProducer) {
            setProducer(entryType, topic, producerName);
        }
    }

    /**
     * send message
     *
     * @param entry message entry
     * @return send result state
     */
    public boolean send(@NotNull T entry) {
        Assert.notNull(entry, "Send entry can't be null");

        if (!isInitProducer) {
            throw new RuntimeException("Please call init() method first");
        }

        try {
            MessageId messageId = producer.send(entry);
            logger.debug("send message success, messageId:{}, entry:{}", messageId, EntryUtils.toString(entry));
            return true;
        } catch (PulsarClientException e) {
            logger.error("send message with exception, entry:{}", EntryUtils.toString(entry), e);
        }

        return false;
    }

    /**
     * send message after the specified relative delay
     * <b>Note</b>: messages are only delivered with delay when a consumer is consuming through a SubscriptionType.Shared subscription. With other subscription types, the messages will still be delivered immediately.
     *
     * @param entry message entry
     * @param delay the amount of delay before the message will be delivered
     * @param unit  the time unit for the delay
     * @return send result state
     */
    public boolean sendAfter(@NotNull T entry, long delay, TimeUnit unit) {
        Assert.notNull(entry, "Send entry can't be null");

        if (!isInitProducer) {
            throw new RuntimeException("Please call init() method first");
        }

        try {
            MessageId messageId = producer.newMessage().value(entry).deliverAfter(delay, unit).send();
            logger.debug("send message success, messageId:{}, entry:{}", messageId, EntryUtils.toString(entry));
            return true;
        } catch (PulsarClientException e) {
            logger.error("send message with exception, entry:{}", EntryUtils.toString(entry), e);
        }

        return false;
    }

    /**
     * send message at the specified timestamp
     * <b>Note</b>: messages are only delivered with delay when a consumer is consuming through a SubscriptionType.Shared subscription. With other subscription types, the messages will still be delivered immediately.
     *
     * @param entry     message entry
     * @param timestamp timestamp is milliseconds and based on UTC
     * @return send result state
     */
    public boolean sendAt(@NotNull T entry, long timestamp) {
        Assert.notNull(entry, "Send entry can't be null");

        if (!isInitProducer) {
            throw new RuntimeException("Please call init() method first");
        }

        try {
            MessageId messageId = producer.newMessage().value(entry).deliverAt(timestamp).send();
            logger.debug("send message success, messageId:{}, timestamp:{}, entry:{}", messageId, timestamp, EntryUtils.toString(entry));
            return true;
        } catch (PulsarClientException e) {
            logger.error("send message with exception, entry:{}", EntryUtils.toString(entry), e);
        }

        return false;
    }

    /* ...... */

    private void setProducer(Class<T> entryType, String topic, String producerName) {
        if (!isInitProducer) {
            Schema<T> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(entryType).build());

            try {
                ProducerBuilder<T> builder = client.newProducer(schema).topic(topic);
                if (producerName != null && !producerName.isEmpty()) {
                    builder.producerName(producerName);
                }

                producer = builder.create();
                isInitProducer = true;
            } catch (PulsarClientException e) {
                logger.error("创建Producer异常, topic:{}, producerName:{}", topic, producerName, e);
                throw new RuntimeException("创建Producer异常", e);
            }
        }
    }
}
