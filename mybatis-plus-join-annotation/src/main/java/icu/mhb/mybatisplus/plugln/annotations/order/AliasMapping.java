package icu.mhb.mybatisplus.plugln.annotations.order;

import java.lang.annotation.*;

/**
 * @author mahuibo
 * @Title: AliasMapping
 * @email mhb0409@qq.com
 * @time 2025/2/27
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AliasMapping {

    /**
     * 字段名
     */
    String filedName();

    /**
     * 数据库列名
     */
    String columnName();

    /**
     * 表别名
     */
    String tableAlias();
}
