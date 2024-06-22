package icu.mhb.mybatisplus.plugln.annotations;
import java.lang.annotation.*;

/**
 * 设置表别名
 *
 * @author mahuibo
 * @Title: TableAlias
 * @time 8/21/21 7:02 PM
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface TableAlias {

    String value();

}
