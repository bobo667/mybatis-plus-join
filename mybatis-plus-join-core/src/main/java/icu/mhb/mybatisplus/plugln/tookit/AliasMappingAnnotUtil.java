package icu.mhb.mybatisplus.plugln.tookit;

import icu.mhb.mybatisplus.plugln.annotations.order.AliasMapping;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mahuibo
 * @Title: AliasMappingAnnonUtil
 * @email mhb0409@qq.com
 * @time 2025/2/27
 */
public final class AliasMappingAnnotUtil {

    private final static Map<Class<?>, Map<Field, Map<String, AliasMapping>>> CACHE = new ConcurrentHashMap<>();

    public static void parsing(Class<?> clz, Field field, AliasMapping[] aliasMappings) {

        if (ArrayUtils.isEmpty(aliasMappings)) {
            return;
        }

        Map<Field, Map<String, AliasMapping>> fieldMapMap = CACHE.get(clz);
        if (null == fieldMapMap) {
            fieldMapMap = new ConcurrentHashMap<>();
        }

        if (fieldMapMap.containsKey(field)) {
            return;
        }

        Map<String, AliasMapping> aliasMappingMap = fieldMapMap.get(field);
        if (null == aliasMappingMap) {
            aliasMappingMap = new ConcurrentHashMap<>();
        }
        for (AliasMapping aliasMapping : aliasMappings) {
            aliasMappingMap.put(aliasMapping.filedName(), aliasMapping);
        }
        fieldMapMap.put(field, aliasMappingMap);
        CACHE.put(clz, fieldMapMap);
    }

    public static AliasMapping get(Class<?> clz, Field field, String fieldName) {
        Map<Field, Map<String, AliasMapping>> fieldMapMap = CACHE.get(clz);
        if (null == fieldMapMap || !fieldMapMap.containsKey(field)) {
            return null;
        }

        Map<String, AliasMapping> aliasMappingMap = fieldMapMap.get(field);
        if (null == aliasMappingMap || !aliasMappingMap.containsKey(fieldName)) {
            return null;
        }

        return aliasMappingMap.get(fieldName);
    }

    public static void clear(Class<?> clz) {
        CACHE.remove(clz);
    }

}
