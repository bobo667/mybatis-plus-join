package icu.mhb.mybatisplus.plugln.annotations.order;

import java.lang.annotation.*;

/**
 * 排序注解，使用方式为 字段名,排序方式
 * <p>
 * 例如，数据库字段名 user_name 实体字段名 userName
 * 传入值：userName,asc
 * 结果： order by user_name asc
 * </p>
 *
 * @author mahuibo
 * @Title: OrderBy
 * @email mhb0409@qq.com
 * @time 2025/2/27
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface OrderBy {

    /**
     * 别名映射
     */
    AliasMapping[] aliasMapping() default {};

}
