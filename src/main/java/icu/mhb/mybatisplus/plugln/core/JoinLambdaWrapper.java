package icu.mhb.mybatisplus.plugln.core;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.*;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.func.JoinMethodFunc;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.keyword.DefaultFuncKeyWord;
import icu.mhb.mybatisplus.plugln.keyword.IFuncKeyWord;
import icu.mhb.mybatisplus.plugln.tookit.IdUtil;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.*;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.COMMA;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.NEWLINE;

/**
 * 构建条件对象
 *
 * @author mahuibo
 * @Title: JoinLambdaWrapper
 * @time 8/21/21 5:17 PM
 */
@SuppressWarnings("all")
public class JoinLambdaWrapper<T> extends SupportJoinLambdaWrapper<T, JoinLambdaWrapper<T>>
        implements Query<JoinLambdaWrapper<T>, T, SFunction<T, ?>>, JoinMethodFunc<T> {

    /**
     * 关键字获取
     */
    private IFuncKeyWord funcKeyWord;

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
     * 一对一 构建列表
     */
    @Getter
    private List<OneToOneSelectBuild> oneToOneSelectBuildList = null;

    /**
     * 多对多 构建列表
     */
    @Getter
    private List<ManyToManySelectBuild> manyToManySelectBuildList = null;

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
     * 是否查询主表全部字段 该条件是在没有指定查询字段的时候生效
     */
    private boolean notDefaultSelectAll = false;

    /**
     * 是否添加去重关键字
     */
    private boolean hasDistinct = false;


    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public JoinLambdaWrapper(T entity, String alias) {
        super.setEntity(entity);
        this.initNeed();
        setAlias(alias);
    }

    public JoinLambdaWrapper(T entity) {
        super.setEntity(entity);
        this.initNeed();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(entity)
     */
    public JoinLambdaWrapper(Class<T> entityClass, String alias) {
        super.setEntityClass(entityClass);
        this.initNeed();
        setAlias(alias);
    }

    public JoinLambdaWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        this.initNeed();
    }

    /**
     * 不建议直接 new 该实例，使用 Wrappers.lambdaQuery(...)
     */
    JoinLambdaWrapper(T entity, Class<T> entityClass, SharedString sqlSelect, AtomicInteger paramNameSeq,
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
     * SELECT 部分 SQL 设置
     *
     * @param columns 查询字段
     */
    @SafeVarargs
    @Override
    public final JoinLambdaWrapper<T> select(SFunction<T, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(columnsToString(false, true, columns));
        }
        return typedThis;
    }


    /**
     * 添加去重函数
     */
    public JoinLambdaWrapper<T> distinct() {
        this.hasDistinct = true;
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
        this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(getEntityOrMasterClass()).chooseSelect(predicate));
        return typedThis;
    }

    @Override
    public String getSqlSelect() {

        if (sqlSelectFlag) {
            return sqlSelectCahce.getStringValue();
        }

        if (StringUtils.isBlank(sqlSelect.getStringValue()) && !this.notDefaultSelectAll) {
            selectAll();
        }

        StringBuilder stringValue = new StringBuilder(sqlSelect.getStringValue());

        String joinSelectSql = joinSqlSelect.stream()
                .map(SharedString::getStringValue)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(COMMA));

        // 只有在拥有主表查询字段并且有子表查询的时候才需要加上','分隔符
        if (stringValue.length() > 0 && StringUtils.isNotBlank(joinSelectSql)) {
            stringValue.append(COMMA);
        }

        stringValue.append(joinSelectSql);

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
     * 转换查询Wrapper 会把 查询条件，group，order by，having转换来
     * 注意该方法无法给 多个入参添加别名，例如 orderByDesc("id","id2")
     * 这种别名就会添加错误
     *
     * @param queryWrapper
     * @return
     */
    public JoinLambdaWrapper<T> changeQueryWrapper(AbstractWrapper queryWrapper) {
        MergeSegments mergeSegments = queryWrapper.getExpression();

        String id = IdUtil.getSimpleUUID();
        for (int i = 0; i < mergeSegments.getNormal().size(); i++) {
            ISqlSegment iSqlSegment = mergeSegments.getNormal().get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = iSqlSegment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                mergeSegments.getNormal().remove(iSqlSegment);
                mergeSegments.getNormal().add(i, () -> getAliasAndField(sqlSegment));
            } else {
                // 替换外联表中的参数名字为唯一的
                mergeSegments.getNormal().remove(iSqlSegment);
                mergeSegments.getNormal().add(i, () -> sqlSegment.replaceAll(JoinConstant.MP_PARAMS_NAME, JoinConstant.MP_PARAMS_NAME + StringPool.DOT + id));
            }
        }
        expressionAdd(mergeSegments.getNormal(), null);

        GroupBySegmentList groupBy = mergeSegments.getGroupBy();
        for (int i = 0; i < groupBy.size(); i++) {
            ISqlSegment iSqlSegment = groupBy.get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = iSqlSegment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                mergeSegments.getGroupBy().remove(iSqlSegment);
                mergeSegments.getGroupBy().add(i, () -> getAliasAndField(sqlSegment));
            }
        }
        expressionAdd(mergeSegments.getGroupBy(), GROUP_BY);

        HavingSegmentList having = mergeSegments.getHaving();
        for (int i = 0; i < having.size(); i++) {
            ISqlSegment iSqlSegment = having.get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = iSqlSegment.getSqlSegment();
            if (sqlSegment.contains("#{")) {
                // 替换外联表中的参数名字为唯一的
                mergeSegments.getHaving().remove(iSqlSegment);
                mergeSegments.getHaving().add(i, () -> sqlSegment.replaceAll(JoinConstant.MP_PARAMS_NAME, JoinConstant.MP_PARAMS_NAME + StringPool.DOT + id));
            }
        }
        expressionAdd(mergeSegments.getHaving(), HAVING);

        OrderBySegmentList orderBy = mergeSegments.getOrderBy();
        for (int i = 0; i < orderBy.size(); i++) {
            ISqlSegment iSqlSegment = orderBy.get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = iSqlSegment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                mergeSegments.getOrderBy().remove(iSqlSegment);
                mergeSegments.getOrderBy().add(i, () -> getAliasAndField(sqlSegment));
            }
        }
        expressionAdd(mergeSegments.getOrderBy(), ORDER_BY);

        getParamNameValuePairs().put(id, queryWrapper.getParamNameValuePairs());

        return typedThis;
    }

    private void expressionAdd(AbstractISegmentList list, SqlKeyword sqlKeyword) {
        if (!list.isEmpty()) {
            if (null != sqlKeyword) {
                list.add(0, sqlKeyword);
            }
            ISqlSegment[] iSqlSegmentArrays = new ISqlSegment[list.size()];
            for (int i = 0; i < list.size(); i++) {
                ISqlSegment sqlSegment = list.get(i);
                iSqlSegmentArrays[i] = sqlSegment;
            }
            getExpression().add(iSqlSegmentArrays);
        }
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
        sqlSelect.toEmpty();
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
            sqlBuilder.append(NEWLINE)
                    .append(sql);
        }

        sqlBuilder.append(NEWLINE).append(lastSql.getStringValue());

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
    void setJoinSelect(SharedString... select) {
        joinSqlSelect.addAll(Arrays.asList(select));
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

//    @Override
//    protected void setFieldMappingList(String fieldName, String columns) {
//        TableFieldInfo info = getTableFieldInfoByFieldName(fieldName);
//        if (null != info && (info.getTypeHandler() != null || info.getJdbcType() != null)) {
//            fieldMappingList.add(new FieldMapping(columns, fieldName, new TableFieldInfoExt(info)));
//            return;
//        }
//        fieldMappingList.add(new FieldMapping(columns, fieldName, null));
//    }


    public List<FieldMapping> getFieldMappingList() {
        return this.fieldMappingList;
    }

    public IFuncKeyWord getFuncKeyWord() {
        if (this.funcKeyWord == null) {
            this.funcKeyWord = new DefaultFuncKeyWord();
        }
        return funcKeyWord;
    }

    public JoinLambdaWrapper<T> setFuncKeyWord(IFuncKeyWord funcKeyWord) {
        this.funcKeyWord = funcKeyWord;
        return typedThis;
    }

}
