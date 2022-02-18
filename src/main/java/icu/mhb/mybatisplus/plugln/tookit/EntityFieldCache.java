package icu.mhb.mybatisplus.plugln.tookit;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 实体字段缓存
 *
 * @author mahuibo
 * @Title: EntityFieldCache
 * @email mhb0409@qq.com
 * @date 2022-02-16
 */
public final class EntityFieldCache {

    private EntityFieldCache() {
    }

    private static final Map<String, Class<?>> ENTITY_FIELD_CACHE_MAP = new ConcurrentHashMap<>();

    public static Class<?> computeIfAbsent(String key, Function<String, Class<?>> mappingFunction) {
        return CollectionUtils.computeIfAbsent(ENTITY_FIELD_CACHE_MAP, key, mappingFunction);
    }


}
