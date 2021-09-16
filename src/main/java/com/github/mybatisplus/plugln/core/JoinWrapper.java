package com.github.mybatisplus.plugln.core;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.github.mybatisplus.plugln.core.support.SupportJoinLambdaWrapper;
import com.github.mybatisplus.plugln.entity.HavingBuild;
import com.github.mybatisplus.plugln.enums.SqlExcerpt;
import com.github.mybatisplus.plugln.tookit.IdUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * 多表关联对象
 *
 * @author mahuibo
 * @Title: JoinWrapper
 * @time 8/21/21 6:27 PM
 */
public class JoinWrapper<T, J> extends SupportJoinLambdaWrapper<T, JoinWrapper<T, J>>
        implements Query<JoinWrapper<T, J>, T, SFunction<T, ?>> {

    private JoinLambdaWrapper<J> wrapper;

    /**
     * join字段
     */
    private List<SharedString> sqlJoin = new ArrayList<>();

    /**
     * having 条件列表
     */
    private List<HavingBuild> havingBuildList = null;

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public JoinWrapper(JoinLambdaWrapper<J> wrapper) {
        this((T) null, wrapper);
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    JoinWrapper(T entity, JoinLambdaWrapper<J> wrapper) {
        super.setEntity(entity);
        super.initNeed();
        this.wrapper = wrapper;
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    JoinWrapper(Class<T> entityClass, JoinLambdaWrapper<J> wrapper) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.wrapper = wrapper;
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(...)
     */
    JoinWrapper(T entity, Class<T> entityClass, SharedString sqlSelect, AtomicInteger paramNameSeq,
                Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.sqlSelect = sqlSelect;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    /**
     * SELECT 部分 SQL 设置
     *
     * @param columns 查询字段
     */
    @SafeVarargs
    @Override
    public final JoinWrapper<T, J> select(SFunction<T, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(columnsToString(false, columns));
        }
        return typedThis;
    }


    /**
     * 过滤查询的字段信息(主键除外!)
     * <p>例1: 只要 java 字段名以 "test" 开头的             -> select(i -&gt; i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -> select(TableFieldInfo::isCharSequence)</p>
     * <p>例3: 只要 java 字段没有填充策略的                 -> select(i -&gt; i.getFieldFill() == FieldFill.DEFAULT)</p>
     * <p>例4: 要全部字段                                   -> select(i -&gt; true)</p>
     * <p>例5: 只要主键字段                                 -> select(i -&gt; false)</p>
     *
     * @param predicate 过滤方式
     * @return this
     */
    @Override
    public JoinWrapper<T, J> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        super.setEntityClass(entityClass);
        this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(getEntityOrMasterClass()).chooseSelect(predicate));
        return typedThis;
    }

    @Override
    public String getSqlSelect() {
        return sqlSelect.getStringValue();
    }

    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected JoinWrapper<T, J> instance() {
        return new JoinWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
                                 new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        wrapper = null;
        sqlJoin.clear();
        sqlSelect.toNull();
    }


    /**
     * left join 处理
     *
     * @param joinTableField   需要关联的表字段
     * @param masterTableField 主表关联表字段
     * @return this
     */
    public <F> JoinWrapper<T, J> leftJoin(SFunction<T, Object> joinTableField, SFunction<F, Object> masterTableField) {
        buildJoinSql(joinTableField, masterTableField, SqlExcerpt.LEFT_JOIN);
        return typedThis;
    }

    /**
     * right join 处理
     *
     * @param joinTableField   需要关联的表字段
     * @param masterTableField 主表关联表字段
     * @return this
     */
    public <F> JoinWrapper<T, J> rightJoin(SFunction<T, Object> joinTableField, SFunction<F, Object> masterTableField) {
        buildJoinSql(joinTableField, masterTableField, SqlExcerpt.RIGHT_JOIN);
        return typedThis;
    }

    /**
     * join 处理
     *
     * @param joinTableField   需要关联的表字段
     * @param masterTableField 主表关联表字段
     * @return this
     */
    public <F> JoinWrapper<T, J> join(SFunction<T, Object> joinTableField, SFunction<F, Object> masterTableField) {
        buildJoinSql(joinTableField, masterTableField, SqlExcerpt.JOIN);
        return typedThis;
    }

    /**
     * 重写父类having语句，存入参数值，传递到JoinLambdaWrapper中，避免重复添加
     *
     * @param condition 是否执行
     * @param sqlHaving 执行SQL
     * @param params    参数
     * @return this
     */
    @Override
    public JoinWrapper<T, J> having(boolean condition, String sqlHaving, Object... params) {

        if (havingBuildList == null) {
            havingBuildList = new ArrayList<>();
        }

        HavingBuild havingBuild = HavingBuild.builder()
                .condition(condition)
                .sql(sqlHaving)
                .params(params)
                .build();

        havingBuildList.add(havingBuild);
        return typedThis;
    }

    /**
     * join 后的拼的条件
     *
     * @param field 字段
     * @param val   值
     * @param index 索引，对应的第几个join 从0 开始计算
     * @return this
     */
    public JoinWrapper<T, J> joinAnd(SFunction<T, Object> field, Object val, int index) {
        // 获取列转换SQL
        String column = columnToString(field);

        SharedString sql = sqlJoin.get(index);
        if (sql == null || StringUtils.isBlank(sql.getStringValue())) {
            throw ExceptionUtils.mpe("no such subscript join");
        }

        sql.setStringValue(sql.getStringValue() + String.format(SqlExcerpt.AND.getSql(), column, val));
        sqlJoin.remove(index);
        sqlJoin.add(index, sql);
        return typedThis;
    }

    /**
     * 构建join SQL
     *
     * @param joinTableField   需要关联的表字段
     * @param masterTableField 主表关联表字段
     * @param sqlExcerpt       需要构建的SQL枚举
     */
    private <F> void buildJoinSql(SFunction<T, Object> joinTableField, SFunction<F, Object> masterTableField, SqlExcerpt sqlExcerpt) {
        // 解析方法
        SerializedLambda joinTableResolve = LambdaUtils.resolve(joinTableField);
        SerializedLambda masterTableResolve = LambdaUtils.resolve(masterTableField);

        Class<?> joinTableClass = joinTableResolve.getImplClass();
        Class<?> masterTableClass = masterTableResolve.getImplClass();
        TableInfo joinTableInfo = TableInfoHelper.getTableInfo(joinTableClass);

        Assert.notNull(joinTableInfo, "can not find tableInfo cache for this entity [%s]", joinTableClass.getName());

        // 获取需要join表别名
        String joinTableAlias = getAlias(joinTableClass);
        // 获取主表别名
        String masterTableAlias = getAlias(masterTableClass);

        // 获取字段名字
        String joinColumn = getColumn(joinTableResolve, true);
        String masterColumn = getColumn(masterTableResolve, true);

        SharedString sharedString = SharedString.emptyString();
        sharedString.setStringValue(String.format(sqlExcerpt.getSql(), joinTableInfo.getTableName(), joinTableAlias, joinTableAlias, joinColumn, masterTableAlias, masterColumn));
        sqlJoin.add(sharedString);
    }

    /**
     * join 句子结束
     *
     * @return 主表JoinLambdaWrapper
     */
    public JoinLambdaWrapper<J> end() {
        wrapper.setJoinSelect(sqlSelect);
        wrapper.setJoinSql(sqlJoin);
        wrapper.setOrderBy(expression.getOrderBy());
        wrapper.setGroupBy(expression.getGroupBy());
        wrapper.setHaving(havingBuildList);
        wrapper.setLastSql(lastSql);
        lastSql.toEmpty();
        expression.getOrderBy().clear();
        expression.getHaving().clear();
        expression.getGroupBy().clear();
        wrapper.setJoinConditionSql(getCustomSqlSegment(), IdUtil.getSimpleUUID(), paramNameValuePairs);
        return wrapper;
    }

}
