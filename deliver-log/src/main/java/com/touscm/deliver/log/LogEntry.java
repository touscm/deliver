package com.touscm.deliver.log;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogEntry {
    private String timestamp;
    private String level;
    private String clazz;
    private String method;
    private String line;
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
        String timestamp = StringUtils.standardUTC(event.getTimeStamp()), level = event.getLevel().levelStr, clazz = event.getLoggerName(), method = null, line = null, message = event.getFormattedMessage();

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
