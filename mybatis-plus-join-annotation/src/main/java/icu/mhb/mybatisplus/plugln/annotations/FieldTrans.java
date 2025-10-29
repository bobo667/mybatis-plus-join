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
public @interface FieldTrans {

    /**
     * 目标子表类 对应Mp实体
     */
    Class<?> targetSub();

    /**
     * 目标子表类 对应Mp主键,默认去拿对应表的Id
     */
    String targetSubId() default "id";

    /**
     * 目标子表类 需要的字段
     * {"userName:userNameStr"}
     * 左边是需要查询的子类表字段，右表是需要映射的字段
     */
    String[] tagetFiledOrRef();

}
