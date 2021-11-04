package com.touscm.deliver.access;

import com.touscm.deliver.base.utils.StringUtils;
import com.touscm.deliver.pulsar.IProducer;
import com.touscm.deliver.pulsar.autoconfigure.PulsarProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * access request deliver by Pulsar
 */
@Service("pulsarAccessDeliver")
public class PulsarAccessDeliver implements IAccessDeliver {
    @Resource
    private PulsarProperties config;
    @Resource
    private IProducer<AccessEntry> accessProducer;

    private boolean isInit = false;
    private static final Object locker = new Object();

    /**
     * deliver access request message
     *
     * @param accessEntry access request entry
     * @return deliver result
     */
    public boolean process(AccessEntry accessEntry) {
        if (accessEntry == null) return false;

        setProducer();
        return accessProducer.send(accessEntry);
    }

    @PreDestroy
    public void close() throws IOException {
        if (accessProducer != null) {
            accessProducer.close();
        }
    }

    /* ...... */

    private void setProducer() {
        synchronized (locker) {
            if (!this.isInit) {
                String topic = config.getAccessTopic();
                if (StringUtils.isEmpty(topic)) throw new RuntimeException("未配置Pulsar请求记录Topic");

                this.accessProducer.init(AccessEntry.class, topic, config.getAccessProducer());
                this.isInit = true;
            }
        }
    }
}
