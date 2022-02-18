package icu.mhb.mybatisplus.plugln.entity;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.tookit.EntityFieldCache;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.springframework.util.ReflectionUtils;

/**
 * @author mahuibo
 * @Title: As
 * @time 9/19/21 4:02 PM
 */
@Getter
public class As<T> {

    /**
     * 列
     */
    SFunction<T, ?> column;

    /**
     * 用户自定义列
     */
    Object columnStr;

    /**
     * 映射的字段名
     */
    String fieldName;

    /**
     * 别名
     */
    String alias;

    /**
     * java类型
     */
    Class<?> javaType;


    /**
     * 不需要别名 使用字段的映射
     *
     * @param column 字段
     */
    @SneakyThrows
    public As(SFunction<T, ?> column) {
        this.column = column;
        this.fieldName = PropertyNamer.methodToProperty(LambdaUtils.extract(column).getImplMethodName());
        this.columnStr = "";
        this.alias = "";
    }

    /**
     * 设置字段名 并添加别名
     *
     * @param column 字段
     * @param alias  别名
     */
    public As(SFunction<T, ?> column, String alias) {
        this.column = column;
        this.fieldName = PropertyNamer.methodToProperty(LambdaUtils.extract(column).getImplMethodName());
        this.alias = alias;
        this.columnStr = "";
    }

    /**
     * 设置字段名 并添加别名、属性名
     *
     * @param column 字段
     * @param alias  别名
     */
    public <F> As(SFunction<T, ?> column, String alias, SFunction<F, ?> fieldName) {
        this.column = column;
        this.fieldName = PropertyNamer.methodToProperty(LambdaUtils.extract(fieldName).getImplMethodName());
        this.alias = alias;
        this.columnStr = "";
    }

    /**
     * 自定义查询字段
     * 例如：
     * As(1, "select_type")
     * select 1 as select_type.....
     *
     * @param columnStr 字段
     * @param alias     别名
     */
    public As(Object columnStr, String alias) {
        this.column = null;
        this.alias = alias;
        this.columnStr = columnStr;
    }


//    private void initFieldAndColumn(SFunction<T, ?> column, SFunction<T, ?> fieldName) {
//        this.column = column;
//        SFunction<T, ?> parsing = column;
//        if (fieldName != null) {
//            parsing = fieldName;
//        }
//        LambdaMeta lambdaMeta = LambdaUtils.extract(parsing);
//        Class<?> metaInstantiatedClass = lambdaMeta.getInstantiatedClass();
//        this.fieldName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());
//        this.javaType = EntityFieldCache.computeIfAbsent(metaInstantiatedClass.getName() + this.fieldName, (v) -> ReflectionUtils.findField(metaInstantiatedClass, this.fieldName).getType());
//    }
//
//    private void initFieldAndColumn(SFunction<T, ?> column) {
//        initFieldAndColumn(column, null);
//    }

}
