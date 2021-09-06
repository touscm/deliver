package com.touscm.deliver.log;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isEmpty() || value.trim().isEmpty();
    }

    /* ...... */

    public static final String STANDARD_UTC_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";

    public static String standardUTC(long timestamp) {
        return new SimpleDateFormat(STANDARD_UTC_FORMAT).format(new Date(timestamp));
    }

    /* ...... */

    private static final Gson gson = new Gson();

    public static <T> String toJson(T entry) {
        if (entry == null) return null;
        if (entry instanceof String) return (String) entry;

        try {
            return gson.toJson(entry);
        } catch (Exception e) {
            logger.error("Json序列化异常, entry:{}", entry, e);
        }
        return null;
    }
}
