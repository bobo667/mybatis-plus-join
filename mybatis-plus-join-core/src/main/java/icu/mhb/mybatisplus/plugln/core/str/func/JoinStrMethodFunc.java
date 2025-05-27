package icu.mhb.mybatisplus.plugln.core.str.func;

import java.util.function.Consumer;

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
    default Children leftJoin(String joinTable, String joinTableField, String masterTableField, String alias) {
        return leftJoin(joinTable, joinTableField, masterTableField, alias, true);
    }

    default Children leftJoin(String joinTable, String joinTableField, String masterTableField, String alias, boolean isLogicDelete) {
        return leftJoin(joinTable, joinTableField, masterTableField, alias, null, isLogicDelete);
    }

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
    default Children leftJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer, boolean isLogicDelete) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.LEFT_JOIN, consumer, isLogicDelete);
    }

    default Children leftJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.LEFT_JOIN, consumer, true);
    }


    /**
     * RIGHT JOIN 带表名和别名
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @return children
     */
    default Children rightJoin(String joinTable, String joinTableField, String masterTableField, String alias) {
        return rightJoin(joinTable, joinTableField, masterTableField, alias, true);
    }

    default Children rightJoin(String joinTable, String joinTableField, String masterTableField, String alias, boolean isLogicDelete) {
        return rightJoin(joinTable, joinTableField, masterTableField, alias, null, isLogicDelete);
    }

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
    default Children rightJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer, boolean isLogicDelete) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.RIGHT_JOIN, consumer, isLogicDelete);
    }

    default Children rightJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.RIGHT_JOIN, consumer, true);
    }


    /**
     * INNER JOIN 带表名和别名
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @return children
     */
    default Children innerJoin(String joinTable, String joinTableField, String masterTableField, String alias) {
        return innerJoin(joinTable, joinTableField, masterTableField, alias, true);
    }

    default Children innerJoin(String joinTable, String joinTableField, String masterTableField, String alias, boolean isLogicDelete) {
        return innerJoin(joinTable, joinTableField, masterTableField, alias, null, isLogicDelete);
    }

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
    default Children innerJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer, boolean isLogicDelete) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.INNER_JOIN, consumer, isLogicDelete);
    }

    default Children innerJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<Children> consumer) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.INNER_JOIN, consumer, true);
    }


    /**
     * 通用JOIN方法 带表名、别名和回调函数
     *
     * @param joinTable        关联表名
     * @param joinTableField   关联表字段
     * @param masterTableField 主表关联字段
     * @param alias            关联表别名
     * @param joinType         连接类型
     * @param consumer         回调函数
     * @param isLogicDelete
     * @return children
     */
    Children join(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType, Consumer<Children> consumer, boolean isLogicDelete);
}
