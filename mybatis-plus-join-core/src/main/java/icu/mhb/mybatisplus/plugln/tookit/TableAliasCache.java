package icu.mhb.mybatisplus.plugln.tookit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 表别名缓存
 *
 * @author mahuibo
 * @Title: TableAliasCache
 * @time 8/27/21 4:00 PM
 */
public final class TableAliasCache {

    private TableAliasCache() {
    }

    private static final Map<Class<?>, String> TABLE_ALIAS_CACHE = new ConcurrentHashMap<>();

    /**
     * 存入或者获取一个数据
     * 如果不存在则存入然后返回，存在则获取
     *
     * @param key             key
     * @param mappingFunction 执行的函数
     * @return 别名
     */
    public static String getOrSet(Class<?> key, Function<Class<?>, String> mappingFunction) {
        return TABLE_ALIAS_CACHE.computeIfAbsent(key, mappingFunction);
    }

}
