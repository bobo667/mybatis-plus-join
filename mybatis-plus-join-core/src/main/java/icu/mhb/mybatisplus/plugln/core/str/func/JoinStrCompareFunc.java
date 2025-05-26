package icu.mhb.mybatisplus.plugln.core.str.func;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * String类型Join条件比较函数接口
 *
 * @author mahuibo
 * @Title: JoinStrCompareFunc
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public interface JoinStrCompareFunc<Children> {

//    /**
//     * 等于 =
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children eq(boolean condition, String column, Object val);
//
//    /**
//     * 等于 =
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children eq(String column, Object val) {
//        return eq(true, column, val);
//    }
//
//    /**
//     * 不等于 <>
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children ne(boolean condition, String column, Object val);
//
//    /**
//     * 不等于 <>
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children ne(String column, Object val) {
//        return ne(true, column, val);
//    }
//
//    /**
//     * 大于 >
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children gt(boolean condition, String column, Object val);
//
//    /**
//     * 大于 >
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children gt(String column, Object val) {
//        return gt(true, column, val);
//    }
//
//    /**
//     * 大于等于 >=
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children ge(boolean condition, String column, Object val);
//
//    /**
//     * 大于等于 >=
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children ge(String column, Object val) {
//        return ge(true, column, val);
//    }
//
//    /**
//     * 小于 <
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children lt(boolean condition, String column, Object val);
//
//    /**
//     * 小于 <
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children lt(String column, Object val) {
//        return lt(true, column, val);
//    }
//
//    /**
//     * 小于等于 <=
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children le(boolean condition, String column, Object val);
//
//    /**
//     * 小于等于 <=
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children le(String column, Object val) {
//        return le(true, column, val);
//    }
//
//    /**
//     * BETWEEN 值1 AND 值2
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val1      值1
//     * @param val2      值2
//     * @return children
//     */
//    Children between(boolean condition, String column, Object val1, Object val2);
//
//    /**
//     * BETWEEN 值1 AND 值2
//     *
//     * @param column 字段
//     * @param val1   值1
//     * @param val2   值2
//     * @return children
//     */
//    default Children between(String column, Object val1, Object val2) {
//        return between(true, column, val1, val2);
//    }
//
//    /**
//     * NOT BETWEEN 值1 AND 值2
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val1      值1
//     * @param val2      值2
//     * @return children
//     */
//    Children notBetween(boolean condition, String column, Object val1, Object val2);
//
//    /**
//     * NOT BETWEEN 值1 AND 值2
//     *
//     * @param column 字段
//     * @param val1   值1
//     * @param val2   值2
//     * @return children
//     */
//    default Children notBetween(String column, Object val1, Object val2) {
//        return notBetween(true, column, val1, val2);
//    }
//
//    /**
//     * LIKE '%值%'
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children like(boolean condition, String column, Object val);
//
//    /**
//     * LIKE '%值%'
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children like(String column, Object val) {
//        return like(true, column, val);
//    }
//
//    /**
//     * NOT LIKE '%值%'
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children notLike(boolean condition, String column, Object val);
//
//    /**
//     * NOT LIKE '%值%'
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children notLike(String column, Object val) {
//        return notLike(true, column, val);
//    }
//
//    /**
//     * LIKE '%值'
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children likeLeft(boolean condition, String column, Object val);
//
//    /**
//     * LIKE '%值'
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children likeLeft(String column, Object val) {
//        return likeLeft(true, column, val);
//    }
//
//    /**
//     * LIKE '值%'
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param val       值
//     * @return children
//     */
//    Children likeRight(boolean condition, String column, Object val);
//
//    /**
//     * LIKE '值%'
//     *
//     * @param column 字段
//     * @param val    值
//     * @return children
//     */
//    default Children likeRight(String column, Object val) {
//        return likeRight(true, column, val);
//    }
//
//    /**
//     * IN (值1, 值2, ...)
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param coll      值集合
//     * @return children
//     */
//    Children in(boolean condition, String column, Collection<?> coll);
//
//    /**
//     * IN (值1, 值2, ...)
//     *
//     * @param column 字段
//     * @param coll   值集合
//     * @return children
//     */
//    default Children in(String column, Collection<?> coll) {
//        return in(true, column, coll);
//    }
//
//    /**
//     * NOT IN (值1, 值2, ...)
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @param coll      值集合
//     * @return children
//     */
//    Children notIn(boolean condition, String column, Collection<?> coll);
//
//    /**
//     * NOT IN (值1, 值2, ...)
//     *
//     * @param column 字段
//     * @param coll   值集合
//     * @return children
//     */
//    default Children notIn(String column, Collection<?> coll) {
//        return notIn(true, column, coll);
//    }
//
//    /**
//     * 字段 IS NULL
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @return children
//     */
//    Children isNull(boolean condition, String column);
//
//    /**
//     * 字段 IS NULL
//     *
//     * @param column 字段
//     * @return children
//     */
//    default Children isNull(String column) {
//        return isNull(true, column);
//    }
//
//    /**
//     * 字段 IS NOT NULL
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @return children
//     */
//    Children isNotNull(boolean condition, String column);
//
//    /**
//     * 字段 IS NOT NULL
//     *
//     * @param column 字段
//     * @return children
//     */
//    default Children isNotNull(String column) {
//        return isNotNull(true, column);
//    }
//
//    /**
//     * 分组：GROUP BY 字段
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @return children
//     */
//    Children groupBy(boolean condition, String column);
//
//    /**
//     * 分组：GROUP BY 字段
//     *
//     * @param column 字段
//     * @return children
//     */
//    default Children groupBy(String column) {
//        return groupBy(true, column);
//    }
//
//    /**
//     * 排序：ORDER BY 字段 ASC
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @return children
//     */
//    Children orderByAsc(boolean condition, String column);
//
//    /**
//     * 排序：ORDER BY 字段 ASC
//     *
//     * @param column 字段
//     * @return children
//     */
//    default Children orderByAsc(String column) {
//        return orderByAsc(true, column);
//    }
//
//    /**
//     * 排序：ORDER BY 字段 DESC
//     *
//     * @param condition 执行条件
//     * @param column    字段
//     * @return children
//     */
//    Children orderByDesc(boolean condition, String column);
//
//    /**
//     * 排序：ORDER BY 字段 DESC
//     *
//     * @param column 字段
//     * @return children
//     */
//    default Children orderByDesc(String column) {
//        return orderByDesc(true, column);
//    }

    /**
     * JOIN AND 条件
     *
     * @param condition 执行条件
     * @param alias     别名
     * @param consumer  条件
     * @return children
     */
    Children joinAnd(boolean condition, String alias, Consumer<Children> consumer);

    /**
     * JOIN AND 条件
     *
     * @param alias    别名
     * @param consumer 条件
     * @return children
     */
    default Children joinAnd(String alias, Consumer<Children> consumer) {
        return joinAnd(true, alias, consumer);
    }
}
