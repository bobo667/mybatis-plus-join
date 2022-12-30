package icu.mhb.mybatisplus.plugln.entity;

import org.apache.ibatis.reflection.property.PropertyNamer;

import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import lombok.Getter;
import lombok.SneakyThrows;

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
     * 类类型
     */
    private Class<?> classType;

    /**
     * 是否加单引号
     */
    private boolean ifQuotes;

    /**
     * 不需要别名 使用字段的映射
     *
     * @param column 字段
     */
    @SneakyThrows
    public As(SFunction<T, ?> column) {
        this.column = column;
        LambdaMeta extract = LambdaUtils.extract(column);
        this.classType = extract.getInstantiatedClass();
        this.fieldName = PropertyNamer.methodToProperty(extract.getImplMethodName());
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
        LambdaMeta extract = LambdaUtils.extract(column);
        this.classType = extract.getInstantiatedClass();
//        this.fieldName = PropertyNamer.methodToProperty(extract.getImplMethodName());
        this.alias = alias;
        this.columnStr = "";
    }

    public <J> As(SFunction<T, ?> column, SFunction<J, ?> alias) {
        this.column = column;
        LambdaMeta extract = LambdaUtils.extract(column);
        this.classType = extract.getInstantiatedClass();
//        this.fieldName = PropertyNamer.methodToProperty(extract.getImplMethodName());
        LambdaMeta extractAlias = LambdaUtils.extract(alias);
        this.alias = PropertyNamer.methodToProperty(extractAlias.getImplMethodName());
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
        LambdaMeta extract = LambdaUtils.extract(fieldName);
        this.fieldName = PropertyNamer.methodToProperty(extract.getImplMethodName());
        this.classType = extract.getInstantiatedClass();
        this.alias = alias;
        this.columnStr = "";
    }

    /**
     * 设置字段名 并自动设置别名加上表名前缀、属性名
     *
     * @param column 字段
     * @param alias  别名
     */
    public As(String tableName, SFunction<T, ?> column) {
        LambdaMeta extract = LambdaUtils.extract(column);
        this.fieldName = PropertyNamer.methodToProperty(extract.getImplMethodName());
        this.classType = extract.getInstantiatedClass();
        this.column = column;
        this.alias = tableName + "_" + this.fieldName;
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
        this.ifQuotes = true;
    }

    /**
     * 自定义查询字段
     * 例如：
     * As(1, "select_type")
     * select 1 as select_type.....
     * <p>
     * 如果isQuotes为true
     * As("sum(u.id)","uSum",true)
     * select sum(u.id) as uSum
     *
     * @param columnStr 字段
     * @param alias     别名
     * @param isQuotes  是否加引号
     */
    public As(Object columnStr, String alias, boolean isQuotes) {
        this.column = null;
        this.alias = alias;
        this.columnStr = columnStr;
        this.ifQuotes = isQuotes;
    }

//    private void initFieldAndColumn(SFunction<T, ?> column, SFunction<T, ?> fieldName) {
//        this.column = column;
//        SFunction<T, ?> parsing = column;
//        if (fieldName != null) {
//            parsing = fieldName;
//        }
//        LambdaMeta lambdaMeta = Lambdas.extract(parsing);
//        Class<?> metaInstantiatedClass = lambdaMeta.getInstantiatedClass();
//        this.fieldName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());
//        this.javaType = EntityFieldCache.computeIfAbsent(metaInstantiatedClass.getName() + this.fieldName, (v) -> ReflectionUtils.findField(metaInstantiatedClass, this.fieldName).getType());
//    }
//
//    private void initFieldAndColumn(SFunction<T, ?> column) {
//        initFieldAndColumn(column, null);
//    }

}
