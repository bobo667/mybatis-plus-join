package icu.mhb.mybatisplus.plugln.tookit;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author mahuibo
 * @Title: CollUtil
 * @email mhb0409@qq.com
 * @time 2025/2/20
 */
public class CollUtil {

    public static <E> E get(Collection<E> collection, int index) {
        if (null == collection) {
            return null;
        }

        final int size = collection.size();
        if (0 == size) {
            return null;
        }

        if (index < 0) {
            index += size;
        }

        // 检查越界
        if (index >= size || index < 0) {
            return null;
        }

        if (collection instanceof List) {
            final List<E> list = ((List<E>) collection);
            return list.get(index);
        } else {
            Iterator<E> iterator = collection.iterator();
            while (iterator.hasNext()) {
                index--;
                if (-1 == index) {
                    return iterator.next();
                }
                iterator.next();
            }
        }
        return null;
    }

}
