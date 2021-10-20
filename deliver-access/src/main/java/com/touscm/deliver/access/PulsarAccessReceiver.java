package com.touscm.deliver.access;

import com.touscm.deliver.base.utils.CollectionUtils;
import com.touscm.deliver.base.utils.EntryUtils;
import com.touscm.deliver.base.utils.StringUtils;
import com.touscm.deliver.pulsar.autoconfigure.PulsarProperties;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.apache.pulsar.shade.com.google.common.collect.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * access request receiver by Pulsar
 */
@Service("pulsarAccessReceiver")
public class PulsarAccessReceiver implements IAccessReceiver {
    private static final Logger logger = LoggerFactory.getLogger(PulsarAccessReceiver.class);

    @Resource
    private PulsarProperties config;
    @Resource
    private PulsarClient client;

    private static final Object locker = new Object();
    private Consumer<AccessEntry> consumer;

    private Function<AccessEntry, Boolean> receiver;

    private boolean isInit = false;
    private int executorCount = 0;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    /* ...... */

    /**
     * register receive access entry process
     *
     * @param receiver receive process
     */
    @Override
    public void reg(@NotNull Function<AccessEntry, Boolean> receiver) {
        if (receiver == null) throw new RuntimeException("消息接受处理不能为NULL");
        this.receiver = receiver;
    }

    /**
     * start receive process
     */
    @Override
    public void start() {
        start(1);
    }

    /**
     * start receive process
     *
     * @param executorCount process executor number
     */
    @Override
    public void start(int executorCount) {
        if (receiver == null) throw new RuntimeException("消息接受处理未注册");
        if (isInit) return;

        initConsumer();
        initExecutorService(executorCount);

        for (int i = 0; i < this.executorCount; i++) {
            executorService.scheduleAtFixedRate(this::receiveAccessEntry, 0, 1, TimeUnit.SECONDS);
        }

        isInit = true;
    }

    /**
     * start batch receive access entry
     */
    @Override
    public void startBatch() {
        if (receiver == null) throw new RuntimeException("消息接受处理未注册");
        if (isInit) return;

        initConsumer();

        executorService.scheduleAtFixedRate(() -> {
            Messages<AccessEntry> messages;
            try {
                messages = consumer.batchReceive();
            } catch (Throwable e) {
                logger.error("batch receive message with exception", e);
                return;
            }

            if (messages == null || messages.size() == 0) {
                return;
            }

            List<MessageId> messageIds = Streams.stream(messages.iterator()).filter(message -> {
                AccessEntry entry;
                if ((entry = message.getValue()) == null) {
                    logger.error("receive AccessEntry is null, messageKey:{}", message.getKey());
                    return false;
                }

                try {
                    return receiver.apply(entry);
                } catch (Throwable e) {
                    logger.error("process AccessEntry with exception, messageKey:{}", message.getKey(), e);
                    return false;
                }
            }).map(Message::getMessageId).collect(toList());

            if (CollectionUtils.isEmpty(messageIds)) {
                return;
            }

            try {
                consumer.acknowledge(messageIds);
            } catch (Throwable e) {
                for (MessageId messageId : messageIds) {
                    try {
                        consumer.negativeAcknowledge(messageId);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        isInit = true;
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        executorService.shutdown();
        if (client != null) {
            client.close();
        }
        if (consumer != null) {
            consumer.close();
        }
    }

    /* ...... */

    private void initConsumer() {
        synchronized (locker) {
            if (consumer == null) {
                String topic = config.getAccessTopic();
                if (StringUtils.isEmpty(topic)) throw new RuntimeException("未配置Pulsar请求记录Topic");

                String subscribe = StringUtils.isEmpty(config.getAccessSubscribe()) ? ACCESS_SUBSCRIBE : config.getAccessSubscribe();
                Schema<AccessEntry> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(AccessEntry.class).build());

                try {
                    consumer = client.newConsumer(schema).topic(topic).subscriptionName(subscribe).subscriptionType(SubscriptionType.Exclusive).subscriptionInitialPosition(SubscriptionInitialPosition.Earliest).subscribe();
                } catch (PulsarClientException e) {
                    logger.error("创建Consumer异常, topic:{}, subscribe:{}", topic, subscribe, e);
                    throw new RuntimeException("创建Consumer异常", e);
                }
            }
        }
    }

    private void initExecutorService(int executorCount) {
        if (executorCount <= 0) {
            this.executorCount = 1;
        } else {
            int max;
            if ((max = Runtime.getRuntime().availableProcessors()) < executorCount) {
                this.executorCount = max;
            } else {
                this.executorCount = executorCount;
            }
        }

        logger.info("init ExecutorService, count:{}", this.executorCount);
        this.executorService = Executors.newScheduledThreadPool(executorCount);
    }

    private void receiveAccessEntry() {
        Message<AccessEntry> message;
        try {
            message = consumer.receive();
        } catch (Throwable e) {
            logger.error("receive message with exception", e);
            return;
        }

        if (message == null) return;

        AccessEntry accessEntry;
        if ((accessEntry = message.getValue()) == null) {
            logger.error("get message entry error, accessEntry is null, messageKey:{}", message.getKey());
            return;
        }

        boolean isProcessed = false;
        try {
            isProcessed = receiver.apply(accessEntry);
        } catch (Throwable e) {
            logger.error("process message entry with exception, messageKey:{}, messageEntry:{}", message.getKey(), EntryUtils.toString(accessEntry), e);
        }

        if (!isProcessed) return;

        acknowledgeConsume(message);
    }

    private void acknowledgeConsume(Message<AccessEntry> message) {
        try {
            consumer.acknowledge(message.getMessageId());
        } catch (Throwable e) {
            try {
                consumer.negativeAcknowledge(message);
            } catch (Throwable ignored) {
            }
        }
    }
}
