package icu.mhb.mybatisplus.plugln.annotations;

import java.lang.annotation.*;

/**
 * @author mahuibo
 * @Title: JoinChainModel
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface JoinChainModel {

    /**
     * 链式字段方法（即返回 {@code ChainFieldData} 的方法，如 {@code _id()} / {@code idField()}）的命名策略。
     * <p>
     * 默认 {@link FieldMethodNaming#UNDERSCORE_PREFIX}，即下划线前缀（{@code _id}、{@code _ageId}），
     * 与旧版本保持一致，向后兼容；如需更符合 Java 命名规范的写法，
     * 可指定 {@link FieldMethodNaming#FIELD_SUFFIX}。
     *
     * @return 命名策略
     * @see FieldMethodNaming
     */
    FieldMethodNaming fieldMethodNaming() default FieldMethodNaming.UNDERSCORE_PREFIX;

    /**
     * 链式字段方法命名策略。
     */
    enum FieldMethodNaming {

        /**
         * 下划线前缀命名：{@code _id}、{@code _userName}。
         * <p>
         * 默认策略，与旧版本保持一致。
         */
        UNDERSCORE_PREFIX,

        /**
         * camelCase + {@code Field} 后缀命名：{@code idField}、{@code userNameField}。
         * <p>
         * 更符合 Java 命名规范，可读性更好。
         */
        FIELD_SUFFIX

    }

}
