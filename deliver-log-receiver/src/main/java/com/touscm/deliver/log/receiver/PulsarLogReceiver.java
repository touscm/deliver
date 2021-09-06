package com.touscm.deliver.log.receiver;

import com.touscm.deliver.log.LogEntry;
import com.touscm.deliver.log.StringUtils;
import com.touscm.deliver.pulsar.autoconfigure.PulsarProperties;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service("pulsarLogReceiver")
public class PulsarLogReceiver implements ILogReceiver {
    private static final Logger logger = LoggerFactory.getLogger(PulsarLogReceiver.class);

    @Resource
    private PulsarProperties config;
    @Resource
    private PulsarClient client;

    private static final Object locker = new Object();
    private Consumer<LogEntry> consumer;

    private Function<LogEntry, Boolean> receiver;

    private boolean isInitExecutor = false;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public void reg(@NotNull Function<LogEntry, Boolean> receiver) {
        if (receiver == null) throw new RuntimeException("日志消息接受处理不能为NULL");
        this.receiver = receiver;
    }

    @Override
    public void start() {
        if (receiver == null) throw new RuntimeException("消息接受处理未注册");
        if (isInitExecutor) return;

        setConsumer();


        executorService.scheduleAtFixedRate(() -> {
            Message<LogEntry> message;
            try {
                message = consumer.receive();
            } catch (Throwable e) {
                logger.error("接收日志消息异常", e);
                return;
            }

            LogEntry entry = message.getValue();
            if (entry == null) {
                logger.error("接收日志消息出错, 接收结果为NULL, messageKey:{}", message.getKey());
                return;
            }

            boolean isProcessed = false;
            try {
                isProcessed = receiver.apply(entry);
            } catch (Throwable e) {
                logger.error("日志处理异常, messageKey:{}, logEntry:{}", message.getKey(), StringUtils.toJson(entry), e);
            }

            if (!isProcessed) return;

            try {
                // Acknowledge the message so that it can be deleted by the message broker
                consumer.acknowledge(message);
            } catch (Throwable e) {
                // Message failed to process, redeliver later
                try {
                    consumer.negativeAcknowledge(message);
                } catch (Throwable ignored) {
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        isInitExecutor = true;
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

    private void setConsumer() {
        synchronized (locker) {
            if (consumer == null) {
                String topic = config.getLogTopic();
                if (topic == null || topic.isEmpty()) {
                    throw new RuntimeException("未配置Pulsar日志Topic");
                }

                String subscribe = StringUtils.isEmpty(config.getLogSubscribe()) ? SUBSCRIBE : config.getLogSubscribe();
                Schema<LogEntry> schema = DefaultImplementation.newJSONSchema(SchemaDefinition.builder().withPojo(LogEntry.class).build());

                try {
                    consumer = client.newConsumer(schema).topic(topic).subscriptionName(subscribe).subscribe();
                } catch (PulsarClientException e) {
                    logger.error("创建日志Consumer异常, topic:{}, subscribe:{}", topic, subscribe, e);
                    throw new RuntimeException("创建日志Consumer异常", e);
                }
            }
        }
    }
}
