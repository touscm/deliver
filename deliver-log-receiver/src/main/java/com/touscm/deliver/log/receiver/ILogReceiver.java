package com.touscm.deliver.log.receiver;

import com.touscm.deliver.log.LogEntry;

import java.io.Closeable;
import java.util.function.Function;

public interface ILogReceiver extends Closeable {
    String SUBSCRIBE = "log-subscribe";

    void reg(Function<LogEntry, Boolean> receiver);

    void start();
}
