package com.touscm.deliver.access;

import java.io.Closeable;

/**
 * access request deliver
 */
public interface IAccessDeliver extends Closeable {
    /**
     * deliver access request message
     *
     * @param accessEntry access request entry
     * @return deliver result
     */
    boolean process(AccessEntry accessEntry);
}
