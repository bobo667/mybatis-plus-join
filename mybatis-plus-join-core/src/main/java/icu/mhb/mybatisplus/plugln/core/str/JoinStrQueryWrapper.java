package icu.mhb.mybatisplus.plugln.core.str;

import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.core.func.JoinQueryFunc;
import icu.mhb.mybatisplus.plugln.core.str.base.AbstractJoinStrWrapper;
import icu.mhb.mybatisplus.plugln.core.str.support.SupportJoinStrQueryWrapper;
import icu.mhb.mybatisplus.plugln.core.str.func.JoinStrQueryFunc;
import icu.mhb.mybatisplus.plugln.core.str.util.StrQueryWrapperHelper;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.tookit.Lists;
import icu.mhb.mybatisplus.plugln.tookit.fun.FunComm;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ORDER_BY;

/**
 * String类型的Join构造器
 *
 * @author mahuibo
 * @Title: JoinStrQueryWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public class JoinStrQueryWrapper<T> extends AbstractJoinStrWrapper<T, JoinStrQueryWrapper<T>>
        implements JoinStrQueryFunc<T, String, JoinStrQueryWrapper<T>>, JoinQueryFunc<T, String, JoinStrQueryWrapper<T>> {

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
    private SharedString sqlSelectCache = new SharedString();

    /**
     * 关联表的查询字段
     */
    private List<SharedString> joinSqlSelect = new java.util.ArrayList<>();

    /**
     * 关联表条件SQL
     */
    private List<String> joinConditionSql = new java.util.ArrayList<>();

    @Override
    public String getSqlSelect() {
        if (sqlSelectFlag) {
            return sqlSelectCache.getStringValue();
        }

        if (CollectionUtils.isEmpty(sqlSelect) && !this.notDefaultSelectAll) {
            selectAll();
        }

        StringBuilder stringValue = new StringBuilder(sqlSelect.stream()
                .map(SharedString::getStringValue)
                .distinct()
                .collect(Collectors.joining(",")));

        // 处理关联表查询字段
        String joinSelectSql = joinSqlSelect.stream()
                .map(SharedString::getStringValue)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));

        // 只有在拥有主表查询字段并且有子表查询的时候才需要加上','分隔符
        if (stringValue.length() > 0 && StringUtils.isNotBlank(joinSelectSql)) {
            stringValue.append(",");
        }

        stringValue.append(joinSelectSql);

        sqlSelectFlag = true;
        if (hasDistinct) {
            stringValue.insert(0, getFuncKeyWord().distinct() + StringPool.SPACE);
        }
        String selectSql = stringValue.toString();

        sqlSelectCache.setStringValue(selectSql);
        return selectSql;
    }

    @Override
    protected JoinStrQueryWrapper<T> instance() {
        return new JoinStrQueryWrapper<>(getEntity(), getEntityClass(), Lists.newArrayList(), paramNameSeq, paramNameValuePairs,
                new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    /**
     * 构造函数
     */
    public JoinStrQueryWrapper(Class<T> entityClass) {
        this(entityClass, null);
    }

    /**
     * 构造函数
     */
    public JoinStrQueryWrapper(T entity) {
        this(entity, null);
    }

    /**
     * 构造函数
     *
     * @param entity 实体
     * @param alias  别名
     */
    public JoinStrQueryWrapper(T entity, String alias) {
        super.setEntity(entity);
        initializeWrapper(alias, getEntityClass());
    }

    /**
     * 构造函数
     *
     * @param entityClass 实体类
     * @param alias       别名
     */
    public JoinStrQueryWrapper(Class<T> entityClass, String alias) {
        super.setEntityClass(entityClass);
        initializeWrapper(alias, entityClass);
    }

    /**
     * 初始化包装器的通用逻辑
     *
     * @param alias       别名
     * @param entityClass 实体类
     */
    private void initializeWrapper(String alias, Class<T> entityClass) {
        super.initNeed();
        if (StringUtils.isBlank(alias)) {
            alias = getAlias();
        }
        setAlias(alias);
        this.masterTableAlias = getAlias();
        alias2table.put(masterTableAlias, TableInfoHelper.getTableInfo(entityClass).getTableName());
    }

    /**
     * 内部构造函数
     */
    JoinStrQueryWrapper(T entity, Class<T> entityClass, List<SharedString> sqlSelect, AtomicInteger paramNameSeq,
                        Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                        SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.initNeed();
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
     * 全参数公共构造函数 - 用于状态复制
     */
    public JoinStrQueryWrapper(T entity, Class<T> entityClass, List<SharedString> sqlSelect, AtomicInteger paramNameSeq,
                               Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                               SharedString lastSql, SharedString sqlComment, SharedString sqlFirst,
                               String masterTableAlias, boolean notDefaultSelectAll, boolean hasDistinct,
                               List<FieldMapping> fieldMappingList, List<OneToOneSelectBuild> oneToOneSelectBuildList,
                               List<ManyToManySelectBuild> manyToManySelectBuildList,
                               Map<String, SharedString> joinSqlMapping, Map<String, String> alias2table,
                               boolean sqlCacheFlag, SharedString sqlCache, boolean sqlSelectFlag, SharedString sqlSelectCache,
                               List<SharedString> joinSqlSelect, List<String> joinConditionSql) {
        super.initNeed();
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
        this.joinSqlMapping = joinSqlMapping != null ? joinSqlMapping : new java.util.HashMap<>();
        this.alias2table = alias2table != null ? alias2table : new java.util.HashMap<>();
        this.sqlCacheFlag = sqlCacheFlag;
        this.sqlCache = sqlCache;
        this.sqlSelectFlag = sqlSelectFlag;
        this.sqlSelectCache = sqlSelectCache;
        this.joinSqlSelect = joinSqlSelect != null ? joinSqlSelect : new java.util.ArrayList<>();
        this.joinConditionSql = joinConditionSql != null ? joinConditionSql : new java.util.ArrayList<>();
    }

    @Override
    public JoinStrQueryWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        throw Exceptions.mpje("请不要调用该方法！请使用 select(Predicate<TableFieldInfo> predicate)");
    }

    @Override
    public String getJoinSql() {
        if (CollectionUtils.isEmpty(joinSqlMapping)) {
            return "";
        }
        return joinSqlMapping.values().stream()
                .map(SharedString::getStringValue)
                .collect(Collectors.joining(Constants.NEWLINE));
    }

    @Override
    public JoinStrQueryWrapper<T> select(Predicate<TableFieldInfo> predicate) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityOrMasterClass());
        if (tableInfo != null) {
            TableInfoExt tableInfoExt = new TableInfoExt(tableInfo);
            List<String> columns = tableInfoExt.chooseSelect(predicate, getAlias());

            // 添加字段映射
            for (String column : columns) {
                // 解析字段名，去掉表别名前缀
                String fieldName = column;
                if (column.contains(".")) {
                    fieldName = column.substring(column.lastIndexOf(".") + 1);
                }

                // 调用setFieldMappingList设置字段映射
                setFieldMappingList(fieldName, column, getEntityOrMasterClass());
            }

            List<SharedString> strings = Lists.changeList(columns, SharedString::new);
            this.sqlSelect.addAll(strings);
        }
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> select(String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }

        // 为没有点号的字段添加主表别名前缀
        List<String> prefixedColumns = new java.util.ArrayList<>();
        for (String column : columns) {
            String prefixedColumn = checkAndHandleColumn(column);
            prefixedColumns.add(prefixedColumn);

            // 添加字段映射
            if (column.toLowerCase().contains(" as ")) {
                // 处理带AS的字段：SELECT age.id AS ageId
                String[] parts = column.split("(?i) as ");
                String aliasName = parts[1].trim();
                setFieldMappingList(aliasName, prefixedColumn);
            } else {
                // 处理普通字段
                String simpleFieldName = column.contains(".") ?
                        column.substring(column.lastIndexOf(".") + 1) : column;
                setFieldMappingList(simpleFieldName, prefixedColumn);
            }
        }

        List<SharedString> list = Lists.changeList(prefixedColumns, SharedString::new);
        this.sqlSelect.addAll(list);
        return typedThis;
    }

    public JoinStrQueryWrapper<T> select(boolean condition, List<String> columns) {
        FunComm.isTrue(condition, () -> this.select(columns.toArray(new String[0])));
        return typedThis;
    }

    /**
     * 带别名前缀的查询字段
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias   表别名
     * @param columns 字段列表
     * @return this
     */
    public JoinStrQueryWrapper<T> selectWithAlias(String alias, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }

        for (String column : columns) {
            String prefixedColumn = handleColumnPrefix(alias, column);
            String fieldName = column.toLowerCase().contains(" as ") ?
                    column.split("(?i) as ")[1].trim() : column;

            setFieldMappingList(fieldName, prefixedColumn);
            this.sqlSelect.add(new SharedString(prefixedColumn));
        }

        return typedThis;
    }

    public JoinStrQueryWrapper<T> selectAs(String column, String alias) {
        String columnWithAlias = String.format(SqlExcerpt.COLUMNS_AS.getSql(), column, alias);

        // 添加字段映射
        setFieldMappingList(alias, columnWithAlias);

        // 添加到查询字段
        SharedString sharedString = new SharedString(columnWithAlias);
        this.sqlSelect.add(sharedString);

        return typedThis;
    }

    public JoinStrQueryWrapper<T> orderBySql(String orderBySql) {
        maybeDo(true, () -> appendSqlSegments(ORDER_BY, columnToSqlSegment(orderBySql)));
        return typedThis;
    }

    public JoinStrQueryWrapper<T> selectSub(String selectSql, String alias) {
        String subQuery = String.format("(%s) %s", selectSql, alias);
        SharedString sharedString = new SharedString(subQuery);
        this.sqlSelect.add(sharedString);

        // 添加字段映射 - 子查询的字段映射
        setFieldMappingList(alias, alias);

        return typedThis;
    }

    public JoinStrQueryWrapper<T> alias(String alias) {
        this.setAlias(alias);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> join(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType, Consumer<JoinStrQueryWrapper<T>> consumer, boolean isLogicDelete) {
        buildJoinSql(joinTable, joinTableField, masterTableField, alias, joinType, consumer, isLogicDelete);
        return typedThis;
    }

    // 这些方法已经在SupportJoinStrQueryWrapper中实现了字段处理，无需重复实现

    @Override
    public JoinStrQueryWrapper<T> joinAnd(String alias, Consumer<JoinStrQueryWrapper<T>> consumer) {
        super.joinAnd(true, alias, consumer);
        return typedThis;
    }


    // 实现 manyToManySelect 方法

    @Override
    public JoinStrQueryWrapper<T> manyToManySelect(String fieldName, String tableAlias) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(alias2table.getOrDefault(tableAlias, tableAlias));
        Class<?> entityType = tableInfo != null ? tableInfo.getEntityType() : Object.class;
        return createManyToManySelect(fieldName, tableAlias, entityType);
    }

    @Override
    public <P> JoinStrQueryWrapper<T> manyToManySelect(SFunction<P, ?> column, String tableNameOrAlias) {
        try {
            String fieldName = extractFieldNameFromLambda(column);
            Class<?> manyToManyClass = extractGenericTypeFromLambda(column, fieldName);
            return createManyToManySelect(fieldName, tableNameOrAlias, manyToManyClass);
        } catch (Exception e) {
            throw Exceptions.mpje("解析Lambda表达式失败: %s", e.getMessage());
        }
    }

    @Override
    public JoinStrQueryWrapper<T> manyToManySelect(String fieldName, String alias, String... columns) {
        List<FieldMapping> fieldMappings = buildFieldMappingList(alias, true, columns);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(alias2table.getOrDefault(alias, alias));
        Class<?> entityType = tableInfo != null ? tableInfo.getEntityType() : Object.class;

        ManyToManySelectBuild manyToManyBuild = ManyToManySelectBuild.builder()
                .manyToManyField(fieldName)
                .manyToManyClass(entityType)
                .manyToManyPropertyType(java.util.List.class)
                .belongsColumns(fieldMappings)
                .build();

        this.manyToManySelectBuildList.add(manyToManyBuild);
        addSelectByFieldMappings(fieldMappings);
        return typedThis;
    }

    // 实现 oneToOneSelect 方法

    @Override
    public JoinStrQueryWrapper<T> oneToOneSelect(String fieldName, String tableNameOrAlias) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(alias2table.getOrDefault(tableNameOrAlias, tableNameOrAlias));
        Class<?> entityType = tableInfo != null ? tableInfo.getEntityType() : Object.class;
        return createOneToOneSelect(fieldName, tableNameOrAlias, entityType);
    }

    @Override
    public <P> JoinStrQueryWrapper<T> oneToOneSelect(SFunction<P, ?> column, String tableNameOrAlias) {
        try {
            String fieldName = extractFieldNameFromLambda(column);
            Class<?> oneToOneClass = extractFieldTypeFromLambda(column, fieldName);
            return createOneToOneSelect(fieldName, tableNameOrAlias, oneToOneClass);
        } catch (Exception e) {
            throw Exceptions.mpje("解析Lambda表达式失败: %s", e.getMessage());
        }
    }

    @Override
    public JoinStrQueryWrapper<T> oneToOneSelect(String fieldName, String tableNameOrAlias, String... columns) {
        List<FieldMapping> fieldMappings = buildFieldMappingList(tableNameOrAlias, true, columns);

        TableInfo tableInfo = TableInfoHelper.getTableInfo(alias2table.getOrDefault(tableNameOrAlias, tableNameOrAlias));

        // 创建一对一构建对象
        OneToOneSelectBuild oneToOneBuild = OneToOneSelectBuild.builder()
                .oneToOneField(fieldName)
                .oneToOneClass(tableInfo.getEntityType()) // 默认使用Object，实际类型在运行时确定
                .belongsColumns(fieldMappings)
                .build();

        // 添加到构建列表
        this.oneToOneSelectBuildList.add(oneToOneBuild);

        // 添加查询字段
        addSelectByFieldMappings(fieldMappings);

        return typedThis;
    }

    // =============== Getter方法 ===============

    public String getMasterTableAlias() {
        return masterTableAlias;
    }

    public boolean isNotDefaultSelectAll() {
        return notDefaultSelectAll;
    }

    public boolean isHasDistinct() {
        return hasDistinct;
    }

    public List<FieldMapping> getFieldMappingList() {
        return fieldMappingList;
    }

    public List<OneToOneSelectBuild> getOneToOneSelectBuildList() {
        return oneToOneSelectBuildList;
    }

    public List<ManyToManySelectBuild> getManyToManySelectBuildList() {
        return manyToManySelectBuildList;
    }

    public Map<String, SharedString> getJoinSqlMapping() {
        return joinSqlMapping;
    }

    public Map<String, String> getAlias2table() {
        return alias2table;
    }

    public boolean isSqlCacheFlag() {
        return sqlCacheFlag;
    }

    public SharedString getSqlCache() {
        return sqlCache;
    }

    public boolean isSqlSelectFlag() {
        return sqlSelectFlag;
    }

    public SharedString getSqlSelectCache() {
        return sqlSelectCache;
    }

    public List<SharedString> getSqlSelectList() {
        return sqlSelect;
    }

    public AtomicInteger getParamNameSeq() {
        return paramNameSeq;
    }

    public MergeSegments getExpression() {
        return expression;
    }

    public SharedString getLastSql() {
        return lastSql;
    }

    public List<SharedString> getJoinSqlSelect() {
        return joinSqlSelect;
    }

    public List<String> getJoinConditionSql() {
        return joinConditionSql;
    }

    /**
     * 转换为Lambda类型的Join构造器
     * 原模原样复制所有状态
     *
     * @return JoinLambdaWrapper实例
     */
    public icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper<T> toLambda() {
        // 将joinSqlMapping转换为joinSql列表
        List<SharedString> joinSqlList = new java.util.ArrayList<>();
        if (CollectionUtils.isNotEmpty(this.joinSqlMapping)) {
            for (SharedString joinSql : this.joinSqlMapping.values()) {
                joinSqlList.add(new SharedString(joinSql.getStringValue()));
            }
        }

        // 将alias2table转换为aliasMap (Class<?> -> String)
        Map<Class<?>, String> aliasMap = new java.util.HashMap<>();
        if (CollectionUtils.isNotEmpty(this.alias2table)) {
            for (Map.Entry<String, String> entry : this.alias2table.entrySet()) {
                try {
                    TableInfo tableInfo = TableInfoHelper.getTableInfo(entry.getValue());
                    if (tableInfo != null) {
                        aliasMap.put(tableInfo.getEntityType(), entry.getKey());
                    }
                } catch (Exception e) {
                    // 忽略转换失败的情况
                }
            }
        }

        // 使用全参数构造函数创建JoinLambdaWrapper，完整复制状态
        return new icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper<T>(
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
                joinSqlList,
                new java.util.ArrayList<>(this.joinSqlSelect),
                new java.util.ArrayList<>(this.joinConditionSql),
                aliasMap,
                this.sqlCacheFlag,
                new SharedString(this.sqlCache.getStringValue()),
                this.sqlSelectFlag,
                new SharedString(this.sqlSelectCache.getStringValue())
        );
    }

    @Override
    public String getSqlSegment() {
        // 如果存在缓存就返回
        if (sqlCacheFlag) {
            return sqlCache.getStringValue();
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
            sqlBuilder.append(StringPool.SPACE)
                    .append(sql);
        }

        sqlBuilder.append(StringPool.SPACE).append(lastSql.getStringValue());

        String sqlBuilderStr = sqlBuilder.toString();
        sqlCache.setStringValue(sqlBuilderStr);
        sqlCacheFlag = true;
        return sqlBuilderStr;
    }

    // =============== 辅助方法 ===============

    /**
     * 从Lambda表达式中提取字段名
     */
    private <P> String extractFieldNameFromLambda(SFunction<P, ?> column) {
        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        return PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());
    }

    /**
     * 从Lambda表达式中提取泛型类型
     */
    private <P> Class<?> extractGenericTypeFromLambda(SFunction<P, ?> column, String fieldName) {
        try {
            LambdaMeta lambdaMeta = LambdaUtils.extract(column);
            Field field = lambdaMeta.getInstantiatedClass().getDeclaredField(fieldName);
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    return (Class<?>) actualTypeArguments[0];
                }
            }
        } catch (Exception e) {
            // 忽略异常，使用默认类型
        }
        return Object.class;
    }

    /**
     * 从Lambda表达式中提取字段类型
     */
    private <P> Class<?> extractFieldTypeFromLambda(SFunction<P, ?> column, String fieldName) {
        try {
            LambdaMeta lambdaMeta = LambdaUtils.extract(column);
            Field field = lambdaMeta.getInstantiatedClass().getDeclaredField(fieldName);
            return field.getType();
        } catch (Exception e) {
            // 忽略异常，使用默认类型
            return Object.class;
        }
    }

    /**
     * 创建多对多查询构建对象的通用方法
     */
    private JoinStrQueryWrapper<T> createManyToManySelect(String fieldName, String tableNameOrAlias, Class<?> manyToManyClass) {
        List<FieldMapping> fieldMappings = buildFieldMappingList(tableNameOrAlias, true);

        ManyToManySelectBuild manyToManyBuild = ManyToManySelectBuild.builder()
                .manyToManyField(fieldName)
                .manyToManyClass(manyToManyClass)
                .manyToManyPropertyType(java.util.List.class)
                .belongsColumns(fieldMappings)
                .build();

        this.manyToManySelectBuildList.add(manyToManyBuild);
        addSelectByFieldMappings(fieldMappings);
        return typedThis;
    }

    /**
     * 创建一对一查询构建对象的通用方法
     */
    private JoinStrQueryWrapper<T> createOneToOneSelect(String fieldName, String tableNameOrAlias, Class<?> oneToOneClass) {
        List<FieldMapping> fieldMappings = buildFieldMappingList(tableNameOrAlias, true);

        OneToOneSelectBuild oneToOneBuild = OneToOneSelectBuild.builder()
                .oneToOneField(fieldName)
                .oneToOneClass(oneToOneClass)
                .belongsColumns(fieldMappings)
                .build();

        this.oneToOneSelectBuildList.add(oneToOneBuild);
        addSelectByFieldMappings(fieldMappings);
        return typedThis;
    }
}
