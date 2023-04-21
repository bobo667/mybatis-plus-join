package icu.mhb.mybatisplus.plugln.tookit;

import java.lang.reflect.Field;
import java.util.Locale;

import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;

import icu.mhb.mybatisplus.plugln.annotations.MasterTable;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;

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
            // 如果失败就尝试的试一下，把第一个首字母小写再找找，因为有些命名的方式可能是 aKeys 这种不符合规范，导致转换失败
            // 如果第一个字母为大写就转换尝试
            if (Character.isUpperCase(name.charAt(0))) {
                name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
                field = ReflectionKit.getFieldMap(clz).get(name);
            }
            if (field == null) {
                return null;
            }
//            throw Exceptions.mpje("获取【" + clz.getName() + "】中的属性【" + name + "】失败，请检查返回对象中是否存在！");
        }

        return field;
    }

    /**
     * 获取这个类的所有父类和接口是否包含某个类
     *
     * @param clazz
     * @return
     */
    public static boolean hasIncludeClass(Class<?> clazz, Class<?> includeClass) {
        Class<?> suCl = clazz;
        while (suCl != null) {
            if (suCl.equals(includeClass)) {
                return true;
            }
            // 查看这个类继承的接口列表
            for (Class<?> anInterface : suCl.getInterfaces()) {
                if (anInterface.equals(includeClass)) {
                    return true;
                }
            }

            suCl = suCl.getSuperclass();
            // 是否是对应类，如果不是则去看他实现的接口
        }
        return false;
    }


}
