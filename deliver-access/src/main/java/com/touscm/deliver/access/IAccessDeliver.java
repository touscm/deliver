package com.touscm.deliver.access;

import java.io.Closeable;

public interface IAccessDeliver extends Closeable {
    boolean process(AccessEntry accessEntry);
}
