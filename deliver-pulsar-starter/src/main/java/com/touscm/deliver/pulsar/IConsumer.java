package com.touscm.deliver.pulsar;

import com.touscm.deliver.base.constant.ConsumeMode;
import org.apache.pulsar.client.api.SubscriptionType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.function.Function;

public interface IConsumer<T> {
    /**
     * register receive message process
     *
     * @param receiver receive message process
     */
    void reg(@NotNull Function<T, Boolean> receiver);

    /**
     * start receive message
     *
     * @param entryType message entry type
     * @param topic     topic
     * @param subscribe subscribe
     */
    void start(@NotNull Class<T> entryType, @NotBlank String topic, String subscribe);

    /**
     * start receive message
     *
     * @param entryType     message entry type
     * @param consumeMode   run mode
     * @param executorCount process executor number
     * @param topic         topic
     * @param subscribe     subscribe
     */
    void start(@NotNull Class<T> entryType, @NotNull ConsumeMode consumeMode, int executorCount, @NotBlank String topic, String subscribe);

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
    void start(@NotNull Class<T> entryType, @NotNull ConsumeMode consumeMode, SubscriptionType subscriptionType, int executorCount, @NotBlank String topic, String subscribe);
}
