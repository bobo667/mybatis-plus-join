package icu.mhb.mybatisplus.plugln.core;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ASC;
import static com.baomidou.mybatisplus.core.enums.SqlKeyword.DESC;
import static com.baomidou.mybatisplus.core.enums.SqlKeyword.GROUP_BY;
import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ORDER_BY;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.*;
import icu.mhb.mybatisplus.plugln.tookit.Lists;

import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.GroupBySegmentList;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.segments.OrderBySegmentList;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.func.JoinMethodFunc;
import icu.mhb.mybatisplus.plugln.core.func.JoinQueryFunc;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.entity.FieldMapping;
import icu.mhb.mybatisplus.plugln.entity.HavingBuild;
import icu.mhb.mybatisplus.plugln.entity.ManyToManySelectBuild;
import icu.mhb.mybatisplus.plugln.entity.OneToOneSelectBuild;
import icu.mhb.mybatisplus.plugln.entity.OrderByBuild;
import icu.mhb.mybatisplus.plugln.entity.TableInfoExt;
import icu.mhb.mybatisplus.plugln.keyword.DefaultFuncKeyWord;
import icu.mhb.mybatisplus.plugln.keyword.IFuncKeyWord;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 构建条件对象
 *
 * @author mahuibo
 * @Title: JoinLambdaWrapper
 * @time 8/21/21 5:17 PM
 */
@Slf4j
@SuppressWarnings("all")
public class JoinLambdaWrapper<T> extends SupportJoinLambdaWrapper<T, JoinLambdaWrapper<T>>
        implements Query<JoinLambdaWrapper<T>, T, SFunction<T, ?>>, JoinMethodFunc<T>, JoinQueryFunc<T, SFunction<T, ?>, JoinLambdaWrapper<T>> {

    /**
     * 主表class
     */
    private Class<T> masterClass;


    /**
     * 关联表的查询子段
     */
    private List<SharedString> joinSqlSelect = new ArrayList<>();


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
    public JoinLambdaWrapper(T entity, String alias) {
        super.setEntity(entity);
        this.initNeed();
        if (StringUtils.isBlank(alias)) {
            alias = getAlias();
        }
        setAlias(alias);
        this.masterTableAlias = getAlias();
    }

    public JoinLambdaWrapper(T entity) {
        this(entity, null);
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public JoinLambdaWrapper(Class<T> entityClass, String alias) {
        super.setEntityClass(entityClass);
        this.initNeed();
        if (StringUtils.isBlank(alias)) {
            alias = getAlias();
        }
        setAlias(alias);
        this.masterTableAlias = getAlias();
    }

    public JoinLambdaWrapper(Class<T> entityClass) {
        this(entityClass, null);
    }

    JoinLambdaWrapper(T entity, Class<T> entityClass, List<SharedString> sqlSelect, AtomicInteger paramNameSeq,
                      Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                      Map<Class<?>, String> aliasMap,
                      SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.aliasMap = aliasMap;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.sqlSelect = sqlSelect;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    /**
     * 全参数公共构造函数 - 用于状态复制
     */
    public JoinLambdaWrapper(T entity, Class<T> entityClass, List<SharedString> sqlSelect, AtomicInteger paramNameSeq,
                             Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                             SharedString lastSql, SharedString sqlComment, SharedString sqlFirst,
                             String masterTableAlias, boolean notDefaultSelectAll, boolean hasDistinct,
                             List<FieldMapping> fieldMappingList, List<OneToOneSelectBuild> oneToOneSelectBuildList,
                             List<ManyToManySelectBuild> manyToManySelectBuildList,
                             List<SharedString> joinSql, List<SharedString> joinSqlSelect, List<String> joinConditionSql,
                             Map<Class<?>, String> aliasMap, boolean sqlCacheFlag, SharedString sqlCache,
                             boolean sqlSelectFlag, SharedString sqlSelectCache) {
        this.initNeed();
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.sqlSelect = sqlSelect;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
        this.masterTableAlias = masterTableAlias;
        this.notDefaultSelectAll = notDefaultSelectAll;
        this.hasDistinct = hasDistinct;
        this.fieldMappingList = fieldMappingList != null ? fieldMappingList : new java.util.ArrayList<>();
        this.oneToOneSelectBuildList = oneToOneSelectBuildList != null ? oneToOneSelectBuildList : new java.util.ArrayList<>();
        this.manyToManySelectBuildList = manyToManySelectBuildList != null ? manyToManySelectBuildList : new java.util.ArrayList<>();
        this.joinSql = joinSql != null ? joinSql : new java.util.ArrayList<>();
        this.joinSqlSelect = joinSqlSelect != null ? joinSqlSelect : new java.util.ArrayList<>();
        this.joinConditionSql = joinConditionSql != null ? joinConditionSql : new java.util.ArrayList<>();
        this.aliasMap = aliasMap != null ? aliasMap : new java.util.HashMap<>();
        this.sqlCacheFlag = sqlCacheFlag;
        this.sqlCache = sqlCache;
        this.sqlSelectFlag = sqlSelectFlag;
        this.sqlSelectCahce = sqlSelectCache;
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
            this.sqlSelect.addAll(Lists.changeList(columnsToString(false, true, columns), SharedString::new));
        }
        return typedThis;
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
    public JoinLambdaWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        super.setEntityClass(entityClass);
        this.sqlSelect.addAll(Lists.changeList(new TableInfoExt(TableInfoHelper.getTableInfo(getEntityOrMasterClass())).chooseSelect(predicate, getAlias()), SharedString::new));
        return typedThis;
    }

    public List<SharedString> getSqlSelectList() {
        return this.sqlSelect;
    }

    public String getSqlSelect() {

        if (sqlSelectFlag) {
            return sqlSelectCahce.getStringValue();
        }

        if (CollectionUtils.isEmpty(sqlSelect) && !this.notDefaultSelectAll) {
            selectAll();
        }

        StringBuilder stringValue = new StringBuilder(sqlSelect.stream().map(SharedString::getStringValue).distinct().collect(Collectors.joining(",")));

        String joinSelectSql = joinSqlSelect.stream()
                .map(SharedString::getStringValue)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(COMMA));

        // 只有在拥有主表查询字段并且有子表查询的时候才需要加上','分隔符
        if (stringValue.length() > 0 && StringUtils.isNotBlank(joinSelectSql)) {
            stringValue.append(COMMA);
        }

        stringValue.append(joinSelectSql);

        if (CollectionUtils.isNotEmpty(sunQueryList)) {
            // 只有在拥有主表查询字段并且有子表查询的时候才需要加上','分隔符
            if (stringValue.length() > 0) {
                stringValue.append(COMMA);
            }
            stringValue.append(sunQueryList.stream().map(SharedString::getStringValue).collect(Collectors.joining(",")));
        }

        String selectSql = stringValue.toString();

        sqlSelectFlag = true;
        if (hasDistinct) {
            selectSql = getFuncKeyWord().distinct() + " " + selectSql;
        }
        sqlSelectCahce.setStringValue(selectSql);

        return selectSql;
    }

    /**
     * 在没有指定查询主表字段的情况下，不进行查询字段
     */
    public JoinLambdaWrapper<T> notDefaultSelectAll() {
        this.notDefaultSelectAll = true;
        return typedThis;
    }


    /**
     * 用于生成嵌套 sql
     * <p>故 sqlSelect 不向下传递</p>
     */
    @Override
    protected JoinLambdaWrapper<T> instance() {
        return new JoinLambdaWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
                new MergeSegments(), this.getAliasMap(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSelect.clear();
        sqlSelectCahce.toEmpty();
        sqlSelectFlag = false;
        sqlCache.toEmpty();
        sqlCacheFlag = false;
        joinSql.clear();
        joinSqlSelect.clear();
        joinConditionSql.clear();
        aliasMap.clear();
        orderByBuildList.clear();
        fieldMappingList.clear();
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

        if (this.orderByBuildList.size() > 0) {
            // 顺序重排
            this.orderByBuildList = this.orderByBuildList.stream()
                    .sorted(Comparator.comparing(OrderByBuild::getIndex))
                    .collect(Collectors.toList());
            this.orderByBuildList.forEach(i -> {
                // 如果是手写的SQL则不需要 asc desc 排序 开发者自己写
                if (i.isSql()) {
                    maybeDo(i.isCondition(), () -> appendSqlSegments(ORDER_BY, i.getColumn()));
                } else {
                    maybeDo(i.isCondition(), () -> appendSqlSegments(ORDER_BY, i.getColumn(),
                            i.isAsc() ? ASC : DESC));
                }
            });
        }

        String sql = expression.getSqlSegment();
        StringBuilder sqlBuilder = new StringBuilder();

        // 判断实体是否不为空
        boolean nonEmptyOfEntity = this.nonEmptyOfEntity();
        // 判断主表执行SQL是否为空
        boolean sqlIsBlank = StringUtils.isBlank(sql) || expression.getNormal().size() == 0;
        // 判断连表SQL是否为空
        boolean conditionSqlIsNotEmpty = CollectionUtils.isNotEmpty(joinConditionSql);

        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityClass());
        // 如果SQL不为空或者实体不为空就是会创建where字句 否则需要自己手动创建,并且不开启逻辑删除
        if ((sqlIsBlank && !nonEmptyOfEntity) && conditionSqlIsNotEmpty && !tableInfo.isWithLogicDelete()) {
            sqlBuilder.append(Constants.WHERE);
        } else if ((sqlIsBlank && !nonEmptyOfEntity) && conditionSqlIsNotEmpty && tableInfo.isWithLogicDelete()) {
            sqlBuilder.append(Constants.AND);
        } else if (conditionSqlIsNotEmpty && nonEmptyOfEntity && sqlIsBlank) {
            sqlBuilder.append(Constants.AND);
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

        // 如果查询条件为空，并且 排序 、分组、 having 不为空就添加
        if (sqlIsBlank && (expression.getOrderBy().size() > 0 || expression.getGroupBy().size() > 0 || expression.getHaving().size() > 0)) {
            sqlBuilder.append(SPACE)
                    .append(sql);
        }

        sqlBuilder.append(SPACE).append(lastSql.getStringValue());

        String sqlBuilderStr = sqlBuilder.toString();
        sqlCache.setStringValue(sqlBuilderStr);
        sqlCacheFlag = true;
        return sqlBuilderStr;
    }


    @Override
    public <J> JoinWrapper<J, T> join(Class<J> clz, String alias, boolean logicDelete) {
        return new JoinWrapper<>(clz, this, alias, logicDelete);
    }

    /**
     * 存入外联表 join 查询条件
     */
    void setJoinConditionSql(List<SharedString> sunQueryList, List<SharedString> joinSql, String sql, String key, Map<String, Object> joinParamNameValuePairs) {
        // 优化参数检查
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key cannot be blank");
        }

        // 优化参数映射处理
        if (CollectionUtils.isNotEmpty(joinParamNameValuePairs)) {
            paramNameValuePairs.put(key, joinParamNameValuePairs);
        }

        // 优化SQL处理
        if (StringUtils.isNotBlank(sql)) {
            String processedSql = sql.replace(Constants.WHERE, StringPool.SPACE)
                    .replaceAll(JoinConstant.MP_PARAMS_NAME,
                            JoinConstant.MP_PARAMS_NAME + StringPool.DOT + key);
            joinConditionSql.add(processedSql);
        }

        // 优化子查询处理
        if (CollectionUtils.isNotEmpty(sunQueryList)) {
            sunQueryList.forEach(item -> {
                String processedValue = item.getStringValue()
                        .replaceAll(JoinConstant.MP_PARAMS_NAME,
                                JoinConstant.MP_PARAMS_NAME + StringPool.DOT + key);
                item.setStringValue(processedValue);
            });
            this.sunQueryList.addAll(sunQueryList);
        }

        // 优化join SQL处理
        if (CollectionUtils.isNotEmpty(joinSql)) {
            joinSql.forEach(item -> {
                String processedValue = item.getStringValue()
                        .replaceAll(JoinConstant.MP_PARAMS_NAME,
                                JoinConstant.MP_PARAMS_NAME + StringPool.DOT + key);
                item.setStringValue(processedValue);
            });
            this.joinSql.addAll(joinSql);
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
            for (ISqlSegment iSqlSegment : orderBy) {
                this.maybeDo(true, () -> appendSqlSegments(ORDER_BY, iSqlSegment));
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
            for (ISqlSegment iSqlSegment : groupBy) {
                this.maybeDo(true, () -> appendSqlSegments(GROUP_BY, iSqlSegment));
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
    void setJoinSelect(List<SharedString> select) {
        joinSqlSelect.addAll(select);
    }

    void setAliasMap(Map<Class<?>, String> aliasCacheMap) {
        if (CollectionUtils.isNotEmpty(aliasCacheMap)) {
            aliasCacheMap.forEach((k, v) -> aliasMap.put(k, v));
        }
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

    protected void setFieldMappingList(List<FieldMapping> fieldMappingList) {
        if (CollectionUtils.isNotEmpty(fieldMappingList)) {
            this.fieldMappingList.addAll(fieldMappingList);
        }
    }

    void setOderByBuildList(List<OrderByBuild> orderByBuildList) {
        if (CollectionUtils.isNotEmpty(orderByBuildList)) {
            this.orderByBuildList.addAll(orderByBuildList);
        }
    }

    void setSunQueryList(List<SharedString> sunQueryList) {
        if (CollectionUtils.isNotEmpty(sunQueryList)) {
            this.sunQueryList.addAll(sunQueryList);
        }
    }

    void setOneToOneSelect(OneToOneSelectBuild oneToOneSelect) {

        if (null == oneToOneSelect) {
            return;
        }

        if (null == oneToOneSelectBuildList) {
            oneToOneSelectBuildList = new ArrayList<>();
        }

        oneToOneSelectBuildList.add(oneToOneSelect);
    }

    void setManyToManySelect(ManyToManySelectBuild manyToManySelect) {

        if (null == manyToManySelect) {
            return;
        }

        if (null == manyToManySelectBuildList) {
            manyToManySelectBuildList = new ArrayList<>();
        }

        manyToManySelectBuildList.add(manyToManySelect);
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


    /**
     * 转换为String类型的Join构造器
     * 原模原样复制所有状态
     *
     * @return JoinStrQueryWrapper实例
     */
    public icu.mhb.mybatisplus.plugln.core.str.JoinStrQueryWrapper<T> toStr() {

        // 将joinSql列表转换为joinSqlMapping
        Map<String, SharedString> joinSqlMapping = new java.util.HashMap<>();
        if (CollectionUtils.isNotEmpty(this.joinSql)) {
            for (int i = 0; i < this.joinSql.size(); i++) {
                joinSqlMapping.put("join_" + i, new SharedString(this.joinSql.get(i).getStringValue()));
            }
        }

        // 将aliasMap转换为alias2table (String -> String)
        Map<String, String> alias2table = new java.util.HashMap<>();
        if (CollectionUtils.isNotEmpty(this.aliasMap)) {
            for (Map.Entry<Class<?>, String> entry : this.aliasMap.entrySet()) {
                try {
                    TableInfo tableInfo = TableInfoHelper.getTableInfo(entry.getKey());
                    if (tableInfo != null) {
                        alias2table.put(entry.getValue(), tableInfo.getTableName());
                    }
                } catch (Exception e) {
                    // 忽略转换失败的情况
                }
            }
        }

        // 使用全参数构造函数创建JoinStrQueryWrapper，完整复制状态
        return new icu.mhb.mybatisplus.plugln.core.str.JoinStrQueryWrapper<T>(
                getEntity(),
                getEntityClass(),
                new java.util.ArrayList<>(this.sqlSelect),
                this.paramNameSeq,
                this.paramNameValuePairs,
                this.expression,
                this.lastSql,
                this.sqlComment,
                this.sqlFirst,
                this.masterTableAlias,
                this.notDefaultSelectAll,
                this.hasDistinct,
                new java.util.ArrayList<>(this.fieldMappingList),
                new java.util.ArrayList<>(this.oneToOneSelectBuildList),
                new java.util.ArrayList<>(this.manyToManySelectBuildList),
                joinSqlMapping,
                alias2table,
                this.sqlCacheFlag,
                new SharedString(this.sqlCache.getStringValue()),
                this.sqlSelectFlag,
                new SharedString(this.sqlSelectCahce.getStringValue()),
                new java.util.ArrayList<>(this.joinSqlSelect),
                new java.util.ArrayList<>(this.joinConditionSql)
        );
    }
}
