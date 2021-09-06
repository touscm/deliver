package com.touscm.deliver.access;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

public class StringUtils {
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    public static boolean isBlank(String value) {
        return value == null || value.isEmpty() || value.trim().isEmpty();
    }

    /* ...... */

    private static final Gson gson = new Gson();

    public static <T> String toJson(@NotNull T entry) {
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
