package com.touscm.deliver.access;

import com.touscm.deliver.base.constant.ConsumeMode;

import java.io.Closeable;
import java.util.function.Function;

/**
 * access request receiver
 */
public interface IAccessReceiver extends Closeable {
    String ACCESS_SUBSCRIBE = "access-subscribe";

    /**
     * register receive access entry process
     *
     * @param receiver receive process
     */
    void reg(Function<AccessEntry, Boolean> receiver);

    /**
     * start receive process
     */
    void start();

    /**
     * start receive process
     *
     * @param consumeMode   run mode
     * @param executorCount process executor number
     */
    void start(ConsumeMode consumeMode, int executorCount);

    /**
     * start batch receive access entry
     */
    void startBatch();
}
