package com.touscm.deliver.access;

import java.io.Closeable;
import java.util.function.Function;

public interface IAccessReceiver extends Closeable {
    String ACCESS_SUBSCRIBE = "access-subscribe";

    void reg(Function<AccessEntry, Boolean> receiver);

    void start();
}
