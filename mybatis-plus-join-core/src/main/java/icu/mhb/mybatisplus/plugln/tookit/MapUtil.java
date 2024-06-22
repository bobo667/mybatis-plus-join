package icu.mhb.mybatisplus.plugln.tookit;
import java.util.Map;

/**
 * @author mahuibo
 * @Title: MapUtil
 * @email mhb0409@qq.com
 * @time 2021/12/17
 */
public class MapUtil {

    public static <K, V> V getAndRemove(K key, Map<K, V> map) {
        V v = map.get(key);
        map.remove(key);
        return v;
    }

    public static <K, V> void notNullPut(K key, V value, Map<K, V> map) {
        if (null != value) {
            map.put(key, value);
        }
    }

}
