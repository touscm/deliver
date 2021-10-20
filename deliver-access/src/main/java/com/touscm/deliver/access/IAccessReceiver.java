package com.touscm.deliver.access;

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
     * @param executorCount process executor number
     */
    void start(int executorCount);

    /**
     * start batch receive access entry
     */
    void startBatch();
}
