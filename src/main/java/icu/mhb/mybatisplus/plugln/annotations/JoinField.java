package icu.mhb.mybatisplus.plugln.annotations;
import java.lang.annotation.*;

/**
 * 字段join注解标识
 *
 * @author mahuibo
 * @Title: JoinField
 * @email mhb0409@qq.com
 * @time 2022/12/29
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface JoinField {

    /**
     * 主表对象class
     */
    Class<?> masterModelClass();

    /**
     * 子表对象class
     */
    Class<?> sunModelClass();

    /**
     * 主表关联字段，注意不要写别名啥的，就写实体类中的属性名
     */
    String masterModelField();

    /**
     * 子表关联字段，注意不要写别名啥的，就写实体类中的属性名
     */
    String sunModelField();

    /**
     * 子表别名
     */
    String sunAlias() default "";

    /**
     * 关联类型
     */
    String relevancyType();


}
