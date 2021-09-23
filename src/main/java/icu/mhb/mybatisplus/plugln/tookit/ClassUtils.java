package icu.mhb.mybatisplus.plugln.tookit;
import icu.mhb.mybatisplus.plugln.annotations.MasterTable;

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


}
