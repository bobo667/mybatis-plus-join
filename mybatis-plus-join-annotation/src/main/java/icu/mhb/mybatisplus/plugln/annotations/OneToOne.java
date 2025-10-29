package icu.mhb.mybatisplus.plugln.annotations;

import java.lang.annotation.*;

/**
 *
 * @author Ma Huibo
 * @Title: OneToOne
 * @email mhb0409@qq.com
 * @time 2025/10/29
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface OneToOne {

    /**
     * 目标子表类 对应Mp实体
     */
    Class<?> targetSub();

    /**
     * 目标子表类 对应Mp主键,默认去拿对应表的Id
     */
    String targetSubId() default "id";

    /**
     * 映射到哪个字段上
     */
    String ref();

}
