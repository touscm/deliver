package com.touscm.deliver.pulsar.autoconfigure;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({PulsarClient.class})
@EnableConfigurationProperties(PulsarProperties.class)
public class PulsarAutoConfiguration implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(PulsarAutoConfiguration.class);

    private final PulsarProperties pulsarProperties;

    public PulsarAutoConfiguration(PulsarProperties pulsarProperties) {
        this.pulsarProperties = pulsarProperties;
    }

    @Bean
    @ConditionalOnMissingBean({PulsarClient.class})
    public PulsarClient pulsar() {
        try {
            return PulsarClient.builder().serviceUrl(pulsarProperties.getUrl()).build();
        } catch (PulsarClientException e) {
            logger.error("创建PulsarClient异常, url:{}", pulsarProperties.getUrl(), e);
            throw new RuntimeException("创建PulsarClient异常", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
