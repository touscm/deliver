package com.touscm.deliver.base.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * date utils
 */
public class DateUtils {
    /**
     * date time formatter
     */
    public static final FastDateFormat ISO_8601 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss:SSS");

    /**
     * format date string
     *
     * @param date date
     * @return format string
     */
    public static String toString(@NotNull Date date) {
        return ISO_8601.format(date);
    }

    /**
     * format timestamp string
     *
     * @param timestamp timestamp
     * @return format string
     */
    public static String toString(long timestamp) {
        return ISO_8601.format(new Date(timestamp));
    }
}
