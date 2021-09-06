package com.touscm.deliver.access;

import java.io.Closeable;

public interface IAccessDeliver extends Closeable {
    String PRODUCER = "access-producer";

    boolean process(AccessEntry accessEntry);
}
