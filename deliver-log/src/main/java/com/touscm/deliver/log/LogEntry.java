package com.touscm.deliver.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.touscm.deliver.base.utils.DateUtils;

/**
 * log entry
 */
public class LogEntry {
    /**
     * log create time
     */
    private String timestamp;
    /**
     * lob level
     */
    private String level;
    /**
     * log trigger class
     */
    private String clazz;
    /**
     * log trigger method
     */
    private String method;
    /**
     * log trigger line
     */
    private String line;
    /**
     * log message
     */
    private String message;

    /* ...... */

    public LogEntry() {
    }

    public LogEntry(String timestamp, String level, String clazz, String method, String line, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.clazz = clazz;
        this.method = method;
        this.line = line;
        this.message = message;
    }

    /* ...... */

    public static LogEntry parse(ILoggingEvent event) {
        String timestamp = DateUtils.toString(event.getTimeStamp()), level = event.getLevel().levelStr, clazz = event.getLoggerName(), method = null, line = null, message = event.getFormattedMessage();

        StackTraceElement[] callerData = event.getCallerData();
        if (callerData != null && callerData.length > 0) {
            StackTraceElement stackTraceElement = callerData[0];
            method = stackTraceElement.getMethodName();
            line = String.format("%d", stackTraceElement.getLineNumber());
        }

        return new LogEntry(timestamp, level, clazz, method, line, message);
    }

    /* ...... */

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
