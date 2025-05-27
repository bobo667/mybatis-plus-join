package icu.mhb.mybatisplus.plugln.core.str.func;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * String类型Join查询函数接口
 *
 * @author mahuibo
 * @Title: JoinStrQueryFunc
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public interface JoinStrQueryFunc<T, R, Children> {

    /**
     * 获取SELECT SQL
     *
     * @return SELECT SQL
     */
    String getSqlSelect();

    /**
     * 获取JOIN SQL
     *
     * @return JOIN SQL
     */
    String getJoinSql();

    /**
     * 多对多查询 - 使用字符串字段名和表名/别名
     *
     * @param fieldName 映射的Vo字段名
     * @param tableNameOrAlias 表名或别名
     * @return Children
     */
    Children manyToManySelect(String fieldName, String tableNameOrAlias);

    /**
     * 多对多查询 - 使用Lambda表达式和表名/别名
     *
     * @param column Lambda表达式字段
     * @param tableNameOrAlias 表名或别名
     * @return Children
     */
    <P> Children manyToManySelect(SFunction<P, ?> column, String tableNameOrAlias);

    /**
     * 多对多查询 - 使用字符串字段名、表名/别名和指定字段
     *
     * @param fieldName 映射的Vo字段名
     * @param tableNameOrAlias 表名或别名
     * @param columns 要查询的字段名
     * @return Children
     */
    Children manyToManySelect(String fieldName, String tableNameOrAlias, String... columns);

    /**
     * 一对一查询 - 使用字符串字段名和表名/别名
     *
     * @param fieldName 映射的Vo字段名
     * @param tableNameOrAlias 表名或别名
     * @return Children
     */
    Children oneToOneSelect(String fieldName, String tableNameOrAlias);

    /**
     * 一对一查询 - 使用Lambda表达式和表名/别名
     *
     * @param column Lambda表达式字段
     * @param tableNameOrAlias 表名或别名
     * @return Children
     */
    <P> Children oneToOneSelect(SFunction<P, ?> column, String tableNameOrAlias);

    /**
     * 一对一查询 - 使用字符串字段名、表名/别名和指定字段
     *
     * @param fieldName 映射的Vo字段名
     * @param tableNameOrAlias 表名或别名
     * @param columns 要查询的字段名
     * @return Children
     */
    Children oneToOneSelect(String fieldName, String tableNameOrAlias, String... columns);
}
