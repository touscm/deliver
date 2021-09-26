package com.touscm.deliver.access;

import java.io.Closeable;
import java.util.function.Function;

public interface IAccessReceiver extends Closeable {
    String ACCESS_SUBSCRIBE = "access-subscribe";

    /**
     * register receive access entry process
     *
     * @param receiver receive process
     */
    void reg(Function<AccessEntry, Boolean> receiver);

    /**
     * start to receive access entry
     */
    void start();
}
