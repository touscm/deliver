package com.touscm.deliver.base.utils;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class EntryUtils {
    private static final Logger logger = LoggerFactory.getLogger(EntryUtils.class);

    /* ...... */

    public static String getterName(@NotNull Field field) {
        if (field.getType() == Boolean.class || field.getType() == boolean.class) {
            return "is" + field.getName().substring(0, 1).toUpperCase() + (1 < field.getName().length() ? field.getName().substring(1) : "");
        } else {
            return "get" + field.getName().substring(0, 1).toUpperCase() + (1 < field.getName().length() ? field.getName().substring(1) : "");
        }
    }

    public static <T> List<Field> getProperties(@NotNull Class<T> entryType) {
        if (entryType != null && !entryType.isArray() && !entryType.isPrimitive()) {
            List<Field> fields = Arrays.asList(entryType.getDeclaredFields());
            List<Method> methods = Arrays.stream(entryType.getMethods()).filter(a -> a.getParameterCount() == 0).collect(toList());

            return fields.stream().filter(field -> {
                return methods.stream().anyMatch(method -> (method.getName().equals(field.getName()) || method.getName().equals(getterName(field))) && field.getType() == method.getReturnType());
            }).collect(toList());
        }
        return Collections.emptyList();
    }

    public static <T> Object getFieldValue(@NotNull T instance, @NotNull Field field) {
        if (instance != null && field != null) {
            try {
                field.setAccessible(true);
                return field.get(instance);
            } catch (Exception e) {
                logger.error("取得实体字段值异常, instance:{}, field:{}", instance, field.getName(), e);
            }
        }
        return null;
    }

    public static <T> Map<String, Object> toPropertyMap(@NotNull T instance) {
        Map<String, Object> propertyMap = new HashMap<>();
        getProperties(instance.getClass()).forEach(field -> propertyMap.put(field.getName(), getFieldValue(instance, field)));
        return propertyMap;
    }

    /* ...... */

    private static final Gson gson = new Gson();

    public static <T> String toString(@NotNull T entry) {
        if (entry == null) return null;
        if (entry instanceof String) return (String) entry;

        try {
            return gson.toJson(entry);
        } catch (Exception e) {
            logger.error("Json序列化异常, entry:{}", entry, e);
        }
        return null;
    }

    public static <T> T parse(@NotNull Class<T> entryType, @NotBlank String json) {
        if (entryType != null && StringUtils.isNotEmpty(json)) {
            try {
                return gson.fromJson(json, entryType);
            } catch (Exception e) {
                logger.error("Json反序列化异常, type:{}, json:{}", entryType.getName(), json, e);
            }
        }
        return null;
    }

}
