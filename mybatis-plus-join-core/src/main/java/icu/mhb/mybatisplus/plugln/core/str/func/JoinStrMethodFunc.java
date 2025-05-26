package icu.mhb.mybatisplus.plugln.core.str.func;

import java.util.function.Consumer;

import icu.mhb.mybatisplus.plugln.core.str.JoinStrQueryWrapper;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;

/**
 * String类型Join方法函数接口
 *
 * @author mahuibo
 * @Title: JoinStrMethodFunc
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public interface JoinStrMethodFunc<T, Children> {


    /**
     * LEFT JOIN 带表名和别名
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @return children
     */
    Children leftJoin(String joinTable, String joinTableField, String masterTableField, String alias);

    /**
     * LEFT JOIN 带表名、别名和回调函数
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @param consumer         回调函数
     * @return children
     */
    Children leftJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer);


    /**
     * RIGHT JOIN 带表名和别名
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @return children
     */
    Children rightJoin(String joinTable, String joinTableField, String masterTableField, String alias);

    /**
     * RIGHT JOIN 带表名、别名和回调函数
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @param consumer         回调函数
     * @return children
     */
    Children rightJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer);

    /**
     * INNER JOIN 带表名和别名
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @return children
     */
    Children innerJoin(String joinTable, String joinTableField, String masterTableField, String alias);

    /**
     * INNER JOIN 带表名、别名和回调函数
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @param consumer         回调函数
     * @return children
     */
    Children innerJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer);

    /**
     * 通用JOIN方法
     *
     * @param joinTableField   需要关联的表字段
     * @param masterTableField 主表关联表字段
     * @param joinType         连接类型
     * @return children
     */
    Children join(String joinTableField, String masterTableField, SqlExcerpt joinType);

    /**
     * 通用JOIN方法 带表名和别名
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @param joinType         连接类型
     * @return children
     */
    Children join(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType);

    /**
     * 通用JOIN方法 带表名、别名和回调函数
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @param joinType         连接类型
     * @param consumer         回调函数
     * @return children
     */
    Children join(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType, Consumer<Children> consumer);
}
