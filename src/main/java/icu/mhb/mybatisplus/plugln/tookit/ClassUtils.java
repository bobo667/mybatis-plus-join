package icu.mhb.mybatisplus.plugln.tookit;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import icu.mhb.mybatisplus.plugln.annotations.MasterTable;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author mahuibo
 * @Title: ClassUtils
 * @time 8/31/21 5:08 PM
 */
public final class ClassUtils {

    private ClassUtils() {
    }

    /**
     * 获取带MasterTable注解的转换
     *
     * @param clz 需要转换的类
     * @return 转换后的
     */
    public static Class<?> getTableClass(Class<?> clz) {
        if (clz == null) {
            return null;
        }

        MasterTable masterTable = clz.getAnnotation(MasterTable.class);

        if (masterTable != null) {
            return masterTable.value();
        }
        return clz;
    }

    public static Field getDeclaredField(Class<?> clz, String name) {
        Field field = ReflectionKit.getFieldMap(clz).get(name);

        if (field == null) {
            throw new RuntimeException("获取【" + clz.getName() + "】中的属性【" + name + "】失败，请检查返回对象中是否存在！");
        }

        return field;
    }


}
