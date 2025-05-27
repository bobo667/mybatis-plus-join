package icu.mhb.mybatisplus.plugln.core.str.base;

import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import icu.mhb.mybatisplus.plugln.core.str.support.SupportJoinStrQueryWrapper;
import icu.mhb.mybatisplus.plugln.core.str.util.StrQueryWrapperHelper;

import java.util.Collection;
import java.util.function.BiFunction;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ORDER_BY;

/**
 * 抽象Join字符串包装器基类
 * 用于封装重复的条件方法实现，减少代码重复
 *
 * @author mahuibo
 * @Title: AbstractJoinStrWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public abstract class AbstractJoinStrWrapper<T, Children extends AbstractJoinStrWrapper<T, Children>>
        extends SupportJoinStrQueryWrapper<T, Children> {

    /**
     * 处理字段名的通用方法
     * 
     * @param column 字段名
     * @return 处理后的字段名
     */
    protected final String processColumn(String column) {
        return StrQueryWrapperHelper.checkAndHandleColumn(column, masterTableAlias);
    }

    /**
     * 通用的条件方法实现模板
     * 
     * @param condition 执行条件
     * @param column    字段名
     * @param val       值
     * @param conditionFunc 具体的条件函数
     * @return Children
     */
    protected final Children doCondition(boolean condition, String column, Object val,
                                         BiFunction<Boolean, String, Children> conditionFunc) {
        return conditionFunc.apply(condition, processColumn(column));
    }

    /**
     * 通用的双值条件方法实现模板
     * 
     * @param condition 执行条件
     * @param column    字段名
     * @param val1      值1
     * @param val2      值2
     * @param conditionFunc 具体的条件函数
     * @return Children
     */
    protected final Children doCondition(boolean condition, String column, Object val1, Object val2,
                                         QuadFunction<Boolean, String, Object, Object, Children> conditionFunc) {
        return conditionFunc.apply(condition, processColumn(column), val1, val2);
    }

    /**
     * 通用的集合条件方法实现模板
     * 
     * @param condition 执行条件
     * @param column    字段名
     * @param coll      集合值
     * @param conditionFunc 具体的条件函数
     * @return Children
     */
    protected final Children doCollectionCondition(boolean condition, String column, Collection<?> coll,
                                                   TriFunction<Boolean, String, Collection<?>, Children> conditionFunc) {
        return conditionFunc.apply(condition, processColumn(column), coll);
    }

    /**
     * 通用的单字段条件方法实现模板
     * 
     * @param condition 执行条件
     * @param column    字段名
     * @param conditionFunc 具体的条件函数
     * @return Children
     */
    protected final Children doSingleColumnCondition(boolean condition, String column,
                                                     BiFunction<Boolean, String, Children> conditionFunc) {
        return conditionFunc.apply(condition, processColumn(column));
    }

    /**
     * 通用的排序方法实现
     * 
     * @param condition 执行条件
     * @param column    字段名
     * @param direction 排序方向 (ASC/DESC)
     * @return Children
     */
    protected final Children doOrderBy(boolean condition, String column, String direction) {
        return maybeDo(condition, () -> {
            ISqlSegment[] segments = new ISqlSegment[2];
            segments[0] = ORDER_BY;
            segments[1] = () -> columnToString(processColumn(column)) + " " + direction;
            appendSqlSegments(segments);
        });
    }

    // =============== 条件方法实现 ===============

    @Override
    public Children eq(boolean condition, String column, Object val) {
        super.eq(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children ne(boolean condition, String column, Object val) {
        super.ne(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children like(boolean condition, String column, Object val) {
        super.like(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children notLike(boolean condition, String column, Object val) {
        super.notLike(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children likeLeft(boolean condition, String column, Object val) {
        super.likeLeft(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children likeRight(boolean condition, String column, Object val) {
        super.likeRight(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children gt(boolean condition, String column, Object val) {
        super.gt(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children ge(boolean condition, String column, Object val) {
        super.ge(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children lt(boolean condition, String column, Object val) {
        super.lt(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children le(boolean condition, String column, Object val) {
        super.le(condition, processColumn(column), val);
        return typedThis;
    }

    @Override
    public Children between(boolean condition, String column, Object val1, Object val2) {
        super.between(condition, processColumn(column), val1, val2);
        return typedThis;
    }

    @Override
    public Children notBetween(boolean condition, String column, Object val1, Object val2) {
        super.notBetween(condition, processColumn(column), val1, val2);
        return typedThis;
    }

    @Override
    public Children in(boolean condition, String column, Collection<?> coll) {
        super.in(condition, processColumn(column), coll);
        return typedThis;
    }

    @Override
    public Children notIn(boolean condition, String column, Collection<?> coll) {
        super.notIn(condition, processColumn(column), coll);
        return typedThis;
    }

    @Override
    public Children isNull(boolean condition, String column) {
        super.isNull(condition, processColumn(column));
        return typedThis;
    }

    @Override
    public Children isNotNull(boolean condition, String column) {
        super.isNotNull(condition, processColumn(column));
        return typedThis;
    }

    @Override
    public Children groupBy(boolean condition, String column) {
        super.groupBy(condition, processColumn(column));
        return typedThis;
    }

    @Override
    public Children orderByAsc(boolean condition, String column) {
        return doOrderBy(condition, column, "ASC");
    }

    @Override
    public Children orderByDesc(boolean condition, String column) {
        return doOrderBy(condition, column, "DESC");
    }

    /**
     * 三参数函数式接口
     */
    @FunctionalInterface
    protected interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    /**
     * 四参数函数式接口
     */
    @FunctionalInterface
    protected interface QuadFunction<T, U, V, W, R> {
        R apply(T t, U u, V v, W w);
    }
} 