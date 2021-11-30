package icu.mhb.mybatisplus.plugln.enums;
import lombok.Getter;

/**
 * @author mahuibo
 * @Title: PropertyTypeEnum
 * @time 2/6/21 10:06 AM
 */
public enum PropertyType {

    STRING("string", String.class, String.class),
    INT("integer", int.class, Integer.class),
    CHAR("char", char.class, char.class),
    BOOLEAN("boolean", boolean.class, Boolean.class),
    BYTE("byte", byte.class, Byte.class),
    SHORT("short", short.class, Short.class),
    LONG("long", long.class, Long.class),
    DOUBLE("double", double.class, Double.class),
    FLOAT("float", float.class, Float.class);

    @Getter
    private String msg;

    @Getter
    private Class<?> baseType;

    @Getter
    private Class<?> packType;

    /**
     * 获取类型描述
     *
     * @param type 类型
     * @return 类型描述
     */
    public static String getType(Class<?> type) {

        if (STRING.baseType.equals(type) || STRING.packType.equals(type)) {
            return STRING.msg;
        }

        if (INT.baseType.equals(type) || INT.packType.equals(type)) {
            return INT.msg;
        }

        if (CHAR.baseType.equals(type) || CHAR.packType.equals(type)) {
            return CHAR.msg;
        }

        if (BOOLEAN.baseType.equals(type) || BOOLEAN.packType.equals(type)) {
            return BOOLEAN.msg;
        }

        if (BYTE.baseType.equals(type) || BYTE.packType.equals(type)) {
            return BYTE.msg;
        }

        if (LONG.baseType.equals(type) || LONG.packType.equals(type)) {
            return LONG.msg;
        }

        if (DOUBLE.baseType.equals(type) || DOUBLE.packType.equals(type)) {
            return DOUBLE.msg;
        }

        if (FLOAT.baseType.equals(type) || FLOAT.packType.equals(type)) {
            return FLOAT.msg;
        }

        if (SHORT.baseType.equals(type) || SHORT.packType.equals(type)) {
            return SHORT.msg;
        }

        return null;
    }

    /**
     * 判断基础类型
     *
     * @param clz class类型
     * @return 是否是基础类型
     */
    public static boolean hasBaseType(Class<?> clz) {
        return getType(clz) != null;
    }

    PropertyType(String msg, Class<?> baseType, Class<?> packType) {
        this.msg = msg;
        this.baseType = baseType;
        this.packType = packType;
    }

}
