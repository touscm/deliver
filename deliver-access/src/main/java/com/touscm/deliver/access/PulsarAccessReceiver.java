package com.touscm.deliver.access;

import com.touscm.deliver.base.constant.ConsumeMode;
import com.touscm.deliver.base.utils.StringUtils;
import com.touscm.deliver.pulsar.IConsumer;
import com.touscm.deliver.pulsar.autoconfigure.PulsarProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.function.Function;

/**
 * access request receiver by Pulsar
 */
@Service("pulsarAccessReceiver")
public class PulsarAccessReceiver implements IAccessReceiver {
    @Resource
    private PulsarProperties config;
    @Resource
    private IConsumer<AccessEntry> accessConsumer;

    /**
     * register receive access entry process
     *
     * @param receiver receive process
     */
    public void reg(@NotNull Function<AccessEntry, Boolean> receiver) {
        accessConsumer.reg(receiver);
    }

    /**
     * start receive access entry
     */
    public void start() {
        String topic = config.getAccessTopic();
        if (StringUtils.isEmpty(topic)) throw new RuntimeException("未配置Pulsar请求记录Topic");

        accessConsumer.start(AccessEntry.class, ConsumeMode.Longtime, config.getAccessExecutorCount(), topic, config.getAccessSubscribe());
    }

    @PreDestroy
    public void close() throws IOException {
        if (accessConsumer != null) {
            accessConsumer.close();
        }
    }
}
