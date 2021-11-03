package com.touscm.deliver.pulsar;

import com.touscm.deliver.base.constant.ConsumeMode;
import com.touscm.deliver.base.utils.EntryUtils;
import com.touscm.deliver.base.utils.StringUtils;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service("PulsarConsumer")
public class PulsarConsumer<T> implements IConsumer<T> {
    private static final Logger logger = LoggerFactory.getLogger(PulsarConsumer.class);

    @Resource
    private PulsarClient client;

    private Consumer<T> consumer;
    private Function<T, Boolean> receiver;

    private boolean isInit = false;
    private int executorCount = 0;
    private ConsumeMode consumeMode;

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * register receive message process
     *
     * @param receiver receive message process
     */
    public void reg(@NotNull Function<T, Boolean> receiver) {
        Assert.notNull(receiver, "receiver can't be null");

        this.receiver = receiver;
    }

    /**
     * start receive message
     *
     * @param entryType message entry type
     * @param topic     topic
     * @param subscribe subscribe
     */
    public void start(@NotNull Class<T> entryType, @NotBlank String topic, String subscribe) {
        start(entryType, ConsumeMode.Scheduled, SubscriptionType.Exclusive, 1, topic, subscribe);
    }

    /**
     * start receive message
     *
     * @param entryType     message entry type
     * @param consumeMode   run mode
     * @param executorCount process executor number
     * @param topic         topic
     * @param subscribe     subscribe
     */
    public void start(@NotNull Class<T> entryType, @NotNull ConsumeMode consumeMode, int executorCount, @NotBlank String topic, String subscribe) {
        start(entryType, consumeMode, SubscriptionType.Exclusive, executorCount, topic, subscribe);
    }

    /**
     * start receive message
     *
     * @param entryType        message entry type
     * @param consumeMode      run mode
     * @param subscriptionType subscription type
     * @param executorCount    process executor number
     * @param topic            topic
     * @param subscribe        subscribe
     */
    public void start(@NotNull Class<T> entryType, @NotNull ConsumeMode consumeMode, SubscriptionType subscriptionType, int executorCount, @NotBlank String topic, String subscribe) {
        Assert.notNull(entryType, "message entry type can't be null");
        Assert.notNull(consumeMode, "message consume mode can't be null");
        Assert.hasText(topic, "consumer topic can't be empty");

        if (receiver == null) {
            throw new RuntimeException("Please call reg() method first");
        }

        if (isInit) return;

        setConsumer(entryType, subscriptionType, topic, subscribe);
        setExecutorService(consumeMode, executorCount);

        // scheduled receive
        if (ConsumeMode.Scheduled == this.consumeMode) {
            this.scheduledExecutorService.scheduleAtFixedRate(this::receiveEntry, 0, 1, TimeUnit.SECONDS);
            return;
        }

        // long time receive
        for (int i = 0; i < this.executorCount; i++) {
            this.executorService.execute(() -> {
                //noinspection InfiniteLoopStatement
                while (true) {
                    receiveEntry();
                }
            });
        }

        isInit = true;
    }

    /* ...... */

    private void setConsumer(Class<T> entryType, SubscriptionType subscriptionType, String topic, String subscribe) {
        Schema<T> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(entryType).build());
        try {
            ConsumerBuilder<T> consumerBuilder = client.newConsumer(schema).topic(topic);

            if (StringUtils.isNotEmpty(subscribe)) {
                consumerBuilder.subscriptionName(subscribe);
            }
            if (subscriptionType == null) {
                subscriptionType = SubscriptionType.Exclusive;
            }

            this.consumer = consumerBuilder.subscriptionType(subscriptionType).subscriptionInitialPosition(SubscriptionInitialPosition.Earliest).subscribe();
        } catch (PulsarClientException e) {
            logger.error("create consumer with exception, topic:{}, subscribe:{}", topic, subscribe, e);
            throw new RuntimeException("create consumer with exception", e);
        }
    }

    private void setExecutorService(ConsumeMode consumeMode, int executorCount) {
        if (executorCount <= 0) {
            this.executorCount = 1;
        } else {
            int max;
            if ((max = Runtime.getRuntime().availableProcessors() + 1) < executorCount) {
                this.executorCount = max;
            } else {
                this.executorCount = executorCount;
            }
        }

        logger.info("init processor, count:{}", this.executorCount);

        if (ConsumeMode.Longtime == (this.consumeMode = consumeMode)) {
            this.executorService = Executors.newWorkStealingPool(this.executorCount);
        } else {
            this.scheduledExecutorService = Executors.newScheduledThreadPool(this.executorCount);
        }
    }

    private void receiveEntry() {
        Message<T> message;
        try {
            message = this.consumer.receive();
        } catch (Throwable e) {
            logger.error("receive message with exception", e);
            return;
        }

        T entry;
        if ((entry = message.getValue()) == null) {
            logger.error("get message entry error, entry is null, messageKey:{}", message.getKey());
            return;
        }

        boolean isProcessed = false;
        try {
            isProcessed = receiver.apply(entry);
        } catch (Throwable e) {
            logger.error("process message entry with exception, messageKey:{}, entry:{}", message.getKey(), EntryUtils.toString(entry), e);
        }

        if (!isProcessed) return;

        acknowledgeConsume(message);
    }

    private void acknowledgeConsume(Message<T> message) {
        try {
            this.consumer.acknowledge(message.getMessageId());
        } catch (Throwable e) {
            try {
                this.consumer.negativeAcknowledge(message);
            } catch (Throwable ignored) {
            }
        }
    }
}
