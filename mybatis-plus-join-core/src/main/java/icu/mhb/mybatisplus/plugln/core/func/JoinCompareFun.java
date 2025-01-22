package icu.mhb.mybatisplus.plugln.core.func;

import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * join查询条件封装
 * <p>比较值</p>
 *
 * @author mahuibo
 * @Title: JoinCompareFun
 * @email mhb0409@qq.com
 * @time 2022/12/30
 */
public interface JoinCompareFun<Children, T> extends Compare<Children, SFunction<T, ?>> {



    /**
     * ignore
     */
    default <J> Children eq(SFunction<T, Object> column, SFunction<J, Object> val) {
        return eq(true, column, val);
    }

    /**
     * 等于 =
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    <J> Children eq(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val);

    /**
     * ignore
     */
    default <J> Children ne(SFunction<T, Object> column, SFunction<J, Object> val) {
        return ne(true, column, val);
    }

    /**
     * 不等于 &lt;&gt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    <J> Children ne(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val);

    /**
     * ignore
     */
    default <J> Children gt(SFunction<T, Object> column, SFunction<J, Object> val) {
        return gt(true, column, val);
    }

    /**
     * 大于 &gt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    <J> Children gt(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val);

    /**
     * ignore
     */
    default <J> Children ge(SFunction<T, Object> column, SFunction<J, Object> val) {
        return ge(true, column, val);
    }

    /**
     * 大于等于 &gt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    <J> Children ge(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val);

    /**
     * ignore
     */
    default <J> Children lt(SFunction<T, Object> column, SFunction<J, Object> val) {
        return lt(true, column, val);
    }

    /**
     * 小于 &lt;
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    <J> Children lt(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val);

    /**
     * ignore
     */
    default <J> Children le(SFunction<T, Object> column, SFunction<J, Object> val) {
        return le(true, column, val);
    }

    /**
     * 小于等于 &lt;=
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val       值
     * @return children
     */
    <J> Children le(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val);

    /**
     * ignore
     */
    default <J, J2> Children between(SFunction<T, Object> column, SFunction<J, Object> val1, SFunction<J2, Object> val2) {
        return between(true, column, val1, val2);
    }

    /**
     * BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return children
     */
    <J, J2> Children between(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val1, SFunction<J2, Object> val2);

    /**
     * ignore
     */
    default <J, J2> Children notBetween(SFunction<T, Object> column, SFunction<J, Object> val1, SFunction<J2, Object> val2) {
        return notBetween(true, column, val1, val2);
    }

    /**
     * NOT BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return children
     */
    <J, J2> Children notBetween(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val1, SFunction<J2, Object> val2);

}
