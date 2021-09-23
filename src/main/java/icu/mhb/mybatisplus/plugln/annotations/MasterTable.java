package icu.mhb.mybatisplus.plugln.annotations;
import java.lang.annotation.*;

/**
 * 用于vo表之类的关联到主表
 * <span>
 * 例如 -> {
 * UserVo -> User
 * MasterTable(User.class) class UserVo
 * }
 * </span>
 * 即可关联两者
 *
 * @author mahuibo
 * @Title: MasterTable
 * @time 8/31/21 4:44 PM
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface MasterTable {

    /**
     * 需要关联的类class
     */
    Class<?> value();

}
