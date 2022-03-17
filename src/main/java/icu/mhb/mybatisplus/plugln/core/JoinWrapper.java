package icu.mhb.mybatisplus.plugln.core;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.tookit.IdUtil;
import lombok.SneakyThrows;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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
     * 一对一构建
     */
    private OneToOneSelectBuild oneToOneSelectBuild = null;

    /**
     * 多对多构建
     */
    private ManyToManySelectBuild manyToManySelectBuild = null;

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
            this.sqlSelect.setStringValue(columnsToString(false, true, columns));
        }
        return typedThis;
    }

    @SneakyThrows
    public <P> JoinWrapper<T, J> manyToManySelect(SFunction<P, ?> column, Class<?> manyToManyClass, Consumer<ColumnsBuilder<T>> consumer) {

        List<FieldMapping> belongsColumns = buildFiles(column, consumer);

        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        // 获取字段名
        String fieldName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());

        this.manyToManySelectBuild = ManyToManySelectBuild
                .builder()
                .manyToManyField(fieldName)
                .manyToManyPropertyType(lambdaMeta.getInstantiatedClass().getDeclaredField(fieldName).getType())
                .belongsColumns(belongsColumns)
                .manyToManyClass(manyToManyClass)
                .build();

        return typedThis;
    }

    @SneakyThrows
    public <P> JoinWrapper<T, J> oneToOneSelect(SFunction<P, ?> column, Consumer<ColumnsBuilder<T>> consumer) {

        List<FieldMapping> belongsColumns = buildFiles(column, consumer);

        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        // 获取字段名
        String fieldName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());

        this.oneToOneSelectBuild = OneToOneSelectBuild
                .builder()
                .oneToOneField(fieldName)
                .belongsColumns(belongsColumns)
                .oneToOneClass(lambdaMeta.getInstantiatedClass().getDeclaredField(fieldName).getType())
                .build();

        return typedThis;
    }

    private <P> List<FieldMapping> buildFiles(SFunction<P, ?> column, Consumer<ColumnsBuilder<T>> consumer) {
        ColumnsBuilder<T> columnsBuilder = new ColumnsBuilder<>();
        // 执行用户自定义定义
        consumer.accept(columnsBuilder);

        // 查询列
        List<String> selectColumn = new ArrayList<>();

        // 映射列
        List<FieldMapping> belongsColumns = new ArrayList<>();

        for (As<T> as : columnsBuilder.getColumnsBuilderList()) {
            String columnAlias;
            String columnNoAlias = "";
            if (as.getColumn() != null) {
                // 获取序列化后的列明
                String columnToStringNoAlias = columnToStringNoAlias(as.getColumn(), false);
                columnNoAlias = columnToStringNoAlias;
                columnAlias = getAliasAndField(columnToStringNoAlias);
            } else {
                columnAlias = StringUtils.quotaMark(column);
            }

            if (StringUtils.isNotBlank(as.getAlias())) {
                columnNoAlias = as.getAlias();
                columnAlias = String.format(SqlExcerpt.COLUMNS_AS.getSql(), columnAlias, as.getAlias());
            }
            belongsColumns.add(new FieldMapping(columnNoAlias, as.getFieldName()));
            selectColumn.add(columnAlias);
        }

        selectAs(selectColumn);
        return belongsColumns;
    }

    /**
     * 过滤查询的字段信息(主键除外!)
     * <p>例1: 只要 java 字段名以 "test" 开头的             -> select(i -&gt; i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -> select(TableFieldInfoExt::isCharSequence)</p>
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
        oneToOneSelectBuild = null;
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
    public <F> JoinWrapper<T, J> innerJoin(SFunction<T, Object> joinTableField, SFunction<F, Object> masterTableField) {
        buildJoinSql(joinTableField, masterTableField, SqlExcerpt.INNER_JOIN);
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
        LambdaMeta joinTableResolve = LambdaUtils.extract(joinTableField);
        LambdaMeta masterTableResolve = LambdaUtils.extract(masterTableField);

        Class<?> joinTableClass = joinTableResolve.getInstantiatedClass();
        Class<?> masterTableClass = masterTableResolve.getInstantiatedClass();
        TableInfo joinTableInfo = TableInfoHelper.getTableInfo(joinTableClass);

        Assert.notNull(joinTableInfo, "can not find tableInfo cache for this entity [%s]", joinTableClass.getName());

        // 获取需要join表别名
        String joinTableAlias = getAlias(joinTableClass);
        // 获取主表别名
        String masterTableAlias = getAlias(masterTableClass);

        // 获取字段名字
        String joinColumn = getColumn(joinTableResolve, true, false);
        String masterColumn = getColumn(masterTableResolve, true, false);

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
        wrapper.setOneToOneSelect(this.oneToOneSelectBuild);
        wrapper.setManyToManySelect(this.manyToManySelectBuild);
        lastSql.toEmpty();
        expression.getOrderBy().clear();
        expression.getHaving().clear();
        expression.getGroupBy().clear();
        wrapper.setJoinConditionSql(getCustomSqlSegment(), IdUtil.getSimpleUUID(), paramNameValuePairs);
        return wrapper;
    }

}
