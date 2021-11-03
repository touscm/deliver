package com.touscm.deliver.pulsar;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

public interface IProducer<T> {

    /**
     * initialize message entry type and set topic
     *
     * @param entryType    message entry type
     * @param topic        topic
     * @param producerName producer name
     */
    void init(@NotNull Class<T> entryType, @NotBlank String topic, String producerName);

    /**
     * send message
     *
     * @param entry message entry
     * @return send result state
     */
    boolean send(@NotNull T entry);

    /**
     * send message after the specified relative delay<br>
     * <b>Note</b>: messages are only delivered with delay when a consumer is consuming through a SubscriptionType.Shared subscription. With other subscription types, the messages will still be delivered immediately.
     *
     * @param entry message entry
     * @param delay the amount of delay before the message will be delivered
     * @param unit  the time unit for the delay
     * @return send result state
     */
    boolean sendAfter(@NotNull T entry, long delay, TimeUnit unit);

    /**
     * send message at the specified timestamp<br>
     * <b>Note</b>: messages are only delivered with delay when a consumer is consuming through a SubscriptionType.Shared subscription. With other subscription types, the messages will still be delivered immediately.
     *
     * @param entry     message entry
     * @param timestamp timestamp is milliseconds and based on UTC
     * @return send result state
     */
    boolean sendAt(@NotNull T entry, long timestamp);
}
