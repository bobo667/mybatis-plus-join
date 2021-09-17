package com.github.mybatisplus.plugln.core;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.GroupBySegmentList;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.segments.OrderBySegmentList;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.mybatisplus.plugln.core.support.SupportJoinLambdaWrapper;
import com.github.mybatisplus.plugln.constant.JoinConstant;
import com.github.mybatisplus.plugln.entity.HavingBuild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.GROUP_BY;
import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ORDER_BY;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.COMMA;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.NEWLINE;

/**
 * 构建条件对象
 *
 * @author mahuibo
 * @Title: JoinLambdaWrapper
 * @time 8/21/21 5:17 PM
 */
public class JoinLambdaWrapper<T> extends SupportJoinLambdaWrapper<T, JoinLambdaWrapper<T>>
        implements Query<JoinLambdaWrapper<T>, T, SFunction<T, ?>> {


    /**
     * 关联表的查询子段
     */
    private List<SharedString> joinSqlSelect = new ArrayList<>();

    /**
     * 关联表SQL
     */
    private List<SharedString> joinSql = new ArrayList<>();

    /**
     * 关联表条件SQL
     */
    private List<String> joinConditionSql = new ArrayList<>();


    /**
     * 判断SQL是否缓存过
     */
    private boolean sqlCacheFlag;

    /**
     * SQL缓存
     */
    private SharedString sqlCache = new SharedString();

    /**
     * 查询字段是否缓存过
     */
    private boolean sqlSelectFlag;

    /**
     * 查询字段缓存
     */
    private SharedString sqlSelectCahce = new SharedString();


    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public JoinLambdaWrapper(T entity) {
        super.setEntity(entity);
        this.initNeed();
    }


    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public JoinLambdaWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        this.initNeed();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(...)
     */
    JoinLambdaWrapper(T entity, Class<T> entityClass, SharedString sqlSelect, AtomicInteger paramNameSeq,
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
    public final JoinLambdaWrapper<T> select(SFunction<T, ?>... columns) {
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
    public JoinLambdaWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        super.setEntityClass(entityClass);
        this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(getEntityOrMasterClass()).chooseSelect(predicate));
        return typedThis;
    }

    @Override
    public String getSqlSelect() {

        if (sqlSelectFlag) {
            return sqlSelectCahce.getStringValue();
        }

        if (StringUtils.isBlank(sqlSelect.getStringValue())) {
            selectAll();
        }

        StringBuilder stringValue = new StringBuilder(sqlSelect.getStringValue());

        for (SharedString sharedString : joinSqlSelect) {

            if (StringUtils.isBlank(sharedString.getStringValue())) {
                continue;
            }

            if (stringValue.length() != 0) {
                stringValue.append(COMMA);
            }
            stringValue.append(sharedString.getStringValue());
        }

        String selectSql = stringValue.toString();
        // 如果说 没有指定查询语句就默认查询主表的全部字段
//        if (StringUtils.isBlank(selectSql)) {
//            selectAll();
//            selectSql = sqlSelect.getStringValue();
//        }

        sqlSelectFlag = true;
        sqlSelectCahce.setStringValue(selectSql);

        return selectSql;
    }


    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected JoinLambdaWrapper<T> instance() {
        return new JoinLambdaWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
                                       new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSelect.toEmpty();
        sqlSelectCahce.toEmpty();
        sqlSelectFlag = false;
        sqlCache.toEmpty();
        sqlCacheFlag = false;
        joinSql.clear();
        joinSqlSelect.clear();
        joinConditionSql.clear();
    }

    /**
     * 条件SQL
     *
     * @return
     */
    @Override
    public String getSqlSegment() {

        // 如果存在缓存就返回
        if (sqlCacheFlag) {
            return sqlCache.getStringValue();
        }

        String sql = expression.getSqlSegment();
        StringBuilder sqlBuilder = new StringBuilder();

        boolean sqlIsBlank = StringUtils.isBlank(sql) || expression.getNormal().size() == 0;
        boolean conditionSqlIsNotEmpty = CollectionUtils.isNotEmpty(joinConditionSql);

        // 如果SQL不为空就是会创建where字句 否则需要自己手动创建
        if (sqlIsBlank && conditionSqlIsNotEmpty) {
            sqlBuilder.append(Constants.WHERE);
        }

        if (conditionSqlIsNotEmpty) {
            for (int i = 0; i < joinConditionSql.size(); i++) {
                String conditionSql = joinConditionSql.get(i);

                sqlBuilder.append(StringPool.SPACE);

                if (i > 0) {
                    sqlBuilder.append(Constants.AND);
                }
                sqlBuilder.append(conditionSql);
            }
        }

        if (!sqlIsBlank) {
            // 如果条件不为空代表前面已经有后面就跟and
            if (conditionSqlIsNotEmpty) {
                sqlBuilder.append(StringPool.SPACE).append(Constants.AND)
                        .append(StringPool.SPACE);
            }
            sqlBuilder.append(sql);
        }

        sqlBuilder.append(NEWLINE).append(lastSql.getStringValue());

        String sqlBuilderStr = sqlBuilder.toString();
        sqlCache.setStringValue(sqlBuilderStr);
        sqlCacheFlag = true;
        return sqlBuilderStr;
    }


    /**
     * 进行join操作
     *
     * @param clz 外联表class
     * @param <J> 泛型
     * @return JoinWrapper join条件
     */
    public <J> JoinWrapper<J, T> join(Class<J> clz) {
        return new JoinWrapper<>(clz, this);
    }

    /**
     * 存入外联表 join 查询条件
     *
     * @param sql                     条件SQL
     * @param key                     此次外联表唯一标识码
     * @param joinParamNameValuePairs 条件SQL对应的值
     */
    void setJoinConditionSql(String sql, String key, Map<String, Object> joinParamNameValuePairs) {
        if (StringUtils.isNotBlank(sql)) {
            // 向当前参数map存入外联表的值
            paramNameValuePairs.put(key, joinParamNameValuePairs);
            // 外联表如果执行了条件会存在where标签，需要祛除
            sql = sql.replace(Constants.WHERE, StringPool.SPACE);
            // 替换外联表中的参数名字为唯一的
            sql = sql.replaceAll(JoinConstant.MP_PARAMS_NAME, JoinConstant.MP_PARAMS_NAME + StringPool.DOT + key);
            joinConditionSql.add(sql);
        }
    }

    /**
     * 存入外联表join SQL
     *
     * @param sql SQL
     */
    void setJoinSql(List<SharedString> sql) {
        if (CollectionUtils.isNotEmpty(sql)) {
            joinSql.addAll(sql);
        }
    }

    /**
     * 存入排序
     *
     * @param orderBy 排序列表
     */
    void setOrderBy(OrderBySegmentList orderBy) {
        if (!orderBy.isEmpty()) {
            for (ISqlSegment sqlSegment : orderBy) {
                doIt(true, ORDER_BY, sqlSegment);
            }
        }
    }

    /**
     * 存入分组数据
     *
     * @param groupBy 分组列表
     */
    void setGroupBy(GroupBySegmentList groupBy) {
        if (!groupBy.isEmpty()) {
            for (ISqlSegment sqlSegment : groupBy) {
                doIt(true, GROUP_BY, sqlSegment);
            }
        }
    }

    /**
     * 存入having数据
     *
     * @param havingBuildList having 构建列表
     */
    void setHaving(List<HavingBuild> havingBuildList) {
        if (havingBuildList != null && !havingBuildList.isEmpty()) {
            for (HavingBuild havingBuild : havingBuildList) {
                having(havingBuild.isCondition(), havingBuild.getSql(), havingBuild.getParams());
            }
        }
    }


    /**
     * 存入外联表查询子段
     *
     * @param select 查询字段
     */
    void setJoinSelect(SharedString... select) {
        joinSqlSelect.addAll(Arrays.asList(select));
    }

    /**
     * 存入最后一位的SQL 只能存在一个
     *
     * @param last 外联表传入last数据
     */
    void setLastSql(SharedString last) {
        if (StringUtils.isNotBlank(last.getStringValue())) {
            lastSql = last;
        }
    }

    /**
     * 获取join SQL语句
     *
     * @return 构建好的SQL
     */
    public String getJoinSql() {
        StringBuilder sql = new StringBuilder();
        if (CollectionUtils.isNotEmpty(joinSql)) {
            for (SharedString sharedString : joinSql) {
                sql.append(sharedString.getStringValue()).append(NEWLINE);
            }
        }
        return sql.toString();
    }


    @Override
    public String getCustomSqlSegment() {
        return super.getCustomSqlSegment();
    }

    @Override
    protected void initNeed() {
        super.initNeed();
        final Class<T> entityClass = getEntityClass();
    }

}
