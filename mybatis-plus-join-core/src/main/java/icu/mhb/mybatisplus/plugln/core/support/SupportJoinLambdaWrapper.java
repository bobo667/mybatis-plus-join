package icu.mhb.mybatisplus.plugln.core.support;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import icu.mhb.mybatisplus.plugln.annotations.TableAlias;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.core.func.JoinCompareFun;
import icu.mhb.mybatisplus.plugln.core.func.JoinOrderFunc;
import icu.mhb.mybatisplus.plugln.entity.As;
import icu.mhb.mybatisplus.plugln.entity.ColumnsBuilder;
import icu.mhb.mybatisplus.plugln.entity.FieldMapping;
import icu.mhb.mybatisplus.plugln.entity.OrderByBuild;
import icu.mhb.mybatisplus.plugln.enums.DefTableAlias;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.extend.Joins;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;
import icu.mhb.mybatisplus.plugln.tookit.IdUtil;
import icu.mhb.mybatisplus.plugln.tookit.Lists;
import lombok.Getter;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.*;
import static com.baomidou.mybatisplus.core.enums.WrapperKeyword.APPLY;
import static java.util.stream.Collectors.joining;

/**
 * Join lambda解析
 * 重写于mybatis plus 中的LambdaQueryWrapper
 *
 * @author mahuibo
 * @Title: SupportJoinLambdaWrapper
 * @time 8/24/21 6:28 PM
 * @see com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 */
@SuppressWarnings("all")
public abstract class SupportJoinLambdaWrapper<T, Children extends SupportJoinLambdaWrapper<T, Children>> extends SupportJoinWrapper<T, SFunction<T, ?>, Children> implements JoinOrderFunc<Children, SFunction<T, ?>>, JoinCompareFun<Children, T> {

    public AtomicInteger tableIndex = new AtomicInteger(0);

    /**
     * 子查询列表
     */
    protected List<SharedString> sunQueryList = Lists.newArrayList();

    /**
     * 排序构建列表
     */
    protected List<OrderByBuild> orderByBuildList = Lists.newArrayList();

    @Getter
    protected Map<Class<?>, String> aliasMap = new HashMap<>();

    private Map<Class<?>, Map<String, ColumnCache>> columnMap = new HashMap<>();

    private boolean initColumnMap = false;


    @SuppressWarnings("unchecked")
    @Override
    protected String columnsToString(SFunction<T, ?>... columns) {
        return columnsToString(true, columns);
    }

    @SuppressWarnings("unchecked")
    protected String columnsToString(boolean onlyColumn, SFunction<T, ?>... columns) {
        return Arrays.stream(columns).map(i -> columnToString(i, onlyColumn, false)).collect(joining(StringPool.COMMA));
    }

    protected List<String> columnsToString(boolean onlyColumn, boolean saveType, SFunction<T, ?>... columns) {
        return Arrays.stream(columns).map(i -> columnToString(i, onlyColumn, saveType)).collect(Collectors.toList());
    }

    protected List<String> columnsToString(boolean onlyColumn, boolean saveType, List<SFunction<T, ?>> columns) {
        return columns.stream().map(i -> columnToString(i, onlyColumn, saveType)).collect(Collectors.toList());
    }

    @Override
    protected String columnToString(SFunction<T, ?> column) {
        return columnToString(column, true, false);
    }

    protected String columnToStringNoAlias(SFunction<T, ?> column, boolean saveType) {
        return getColumn(LambdaUtils.extract(column), true, saveType);
    }

    protected String columnToString(SFunction<?, ?> column, boolean onlyColumn, boolean saveType) {
        if (customAliasMap.containsKey(column)) {
            return customAliasMap.get(column);
        }

        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        String columnToString = getColumn(lambdaMeta, true, saveType);
        if (StringUtils.isNotBlank(columnToString)) {
            return getAliasAndField(lambdaMeta.getInstantiatedClass(), columnToString);
        }
        return columnToString;
    }

    @Override
    public SFunction<T, ?> getConditionR(Class<?> entityClass, Field field) {
        return (t -> entityClass.getName() + field.getName() + StringPool.AMPERSAND + IdUtil.getSimpleUUID());
    }

    /**
     * 获取当前类的所有查询字段
     */
    public Children selectAll() {
        return selectAll(new ArrayList<>());
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, SFunction<T, ?> column, int index) {
        orderByBuildList.add(new OrderByBuild() {{
            setIndex(index);
            setColumn(columnToSqlSegment(column));
            setCondition(condition);
            setAsc(isAsc);
            setSql(false);
        }});
        return typedThis;
    }

    @Override
    public Children orderBySql(boolean condition, String sql, int index) {
        orderByBuildList.add(new OrderByBuild() {{
            setIndex(index);
            setColumn(() -> sql);
            setCondition(condition);
            setSql(true);
        }});
        return typedThis;
    }

    public <J> Children selectSunQuery(boolean condition, Class<J> clz, Consumer<JoinLambdaWrapper<J>> wrapper) {
        JoinLambdaWrapper<J> lambdaWrapper = Joins.of(clz);
        lambdaWrapper.notDefaultSelectAll();
        lambdaWrapper.aliasMap.putAll(getAliasMap());
        wrapper.accept(lambdaWrapper);
        // 子查询只能有一个查询字段
        if (lambdaWrapper.getFieldMappingList().size() == 0) {
            throw Exceptions.mpje("子查询不能没有查询字段");
        }

        List<SharedString> sqlList = Lists.newArrayList();

        List<SharedString> selectList = lambdaWrapper.getSqlSelectList();

        List<FieldMapping> mappingList = lambdaWrapper.getFieldMappingList();
        for (int i = 0; i < mappingList.size(); i++) {
            FieldMapping fieldMapping = mappingList.get(i);
            String sql = String.format("(SELECT %s FROM %s %s %s %s)", selectList.get(i).getStringValue(), TableInfoHelper.getTableInfo(clz).getTableName(), getAlias(clz), lambdaWrapper.getJoinSql(), StringUtils.isNotBlank(lambdaWrapper.getSqlSegment()) ? " where " + lambdaWrapper.getSqlSegment() : "");
            sql = String.format(SqlExcerpt.COLUMNS_AS.getSql(), sql, fieldMapping.getColumn());
            sqlList.add(new SharedString(sql));
        }

        if (CollectionUtils.isNotEmpty(lambdaWrapper.getParamNameValuePairs())) {
            String key = IdUtil.getSimpleUUID();
            paramNameValuePairs.put(key, lambdaWrapper.getParamNameValuePairs());
            sqlList = sqlList.stream()
                    .map(sql -> sql.setStringValue(sql.getStringValue().replaceAll(JoinConstant.MP_PARAMS_NAME, JoinConstant.MP_PARAMS_NAME + StringPool.DOT + key)))
                    .collect(Collectors.toList());
        }
        sunQueryList.addAll(sqlList);
        return typedThis;
    }

    public <J> Children selectSunQuery(Class<J> clz, Consumer<JoinLambdaWrapper<J>> wrapper) {
        return selectSunQuery(true, clz, wrapper);
    }

    /**
     * 获取当前类所有查询字段 并增加排除
     *
     * @param excludeColumn 需要排除的字段
     * @return Children
     */
    public Children selectAll(Collection<SFunction<T, ?>> excludeColumn) {

        Class<?> clz = getEntityOrMasterClass();

        Assert.notNull(clz, "Can't get the current parser class");

        // 需要排除的字段列表
        List<String> excludeList = excludeColumn.stream().map(i -> columnToString(i, true, false)).collect(Collectors.toList());

        TableInfo tableInfo = TableInfoHelper.getTableInfo(clz);
        List<SharedString> sqlSelectList = tableInfo.getFieldList().stream().filter(TableFieldInfo::isSelect)
                .map(TableFieldInfo::getSqlSelect)
                .map(this::getAliasAndField)
                .filter(str -> !excludeList.contains(str))
                .map(SharedString::new)
                .collect(Collectors.toList());

        tableInfo.getFieldList().stream().filter(TableFieldInfo::isSelect).filter(tableFieldInfo -> !excludeList.contains(getAliasAndField(tableFieldInfo.getSqlSelect()))).forEach(tableFieldInfo -> setFieldMappingList(tableFieldInfo.getProperty(), tableFieldInfo.getColumn()));

        // 不为空代表有主键
        if (tableInfo.havePK()) {
            String keySqlSelect = tableInfo.getKeyColumn();
            String keyField = getAliasAndField(keySqlSelect);
            if (!excludeList.contains(keyField)) {
                sqlSelectList.add(new SharedString(getAliasAndField(keySqlSelect)));
                setFieldMappingList(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
            }
        }

        this.sqlSelect.addAll(sqlSelectList);
        return typedThis;
    }

    /**
     * 存入自定义别名
     *
     * @param alias 别名
     * @return Children
     */
    protected Children setAlias(String alias) {
        aliasMap.put(getEntityOrMasterClass(), alias);
        return typedThis;
    }

    /**
     * 获取类别名
     *
     * @param clz 需要获取别名的类 如果传入为空 就获取当前类的
     * @return 别名
     */
    protected String getAlias(Class<?> clz) {

        if (clz == null) {
            clz = getEntityOrMasterClass();
        }

        if (null == clz) {
            throw Exceptions.mpje("Can't get the current parser class");
        }

        // 自定义别名
        String aliasCache = aliasMap.get(clz);
        if (StringUtils.isNotBlank(aliasCache)) {
            return aliasCache;
        }

        clz = getTableClass(clz);

        final Class<?> finalClz = clz;

        String alias;

        TableAlias tableAlias = finalClz.getAnnotation(TableAlias.class);
        // 如果表名为空
        if (tableAlias == null) {
            if (DefTableAlias.INDEX.equals(mpjConfig.getDefTableAliasType())) {
                return icu.mhb.mybatisplus.plugln.tookit.StringUtils.format(mpjConfig.getDefTableAlias(), tableIndex.addAndGet(1));
            }
            TableInfo tableInfo = TableInfoHelper.getTableInfo(finalClz);
            if (DefTableAlias.FULL_TABLE_NAME.equals(mpjConfig.getDefTableAliasType())) {
                return icu.mhb.mybatisplus.plugln.tookit.StringUtils.format(mpjConfig.getDefTableAlias(), tableInfo.getTableName());
            }

            return tableInfo.getTableName();
        } else {
            return tableAlias.value();
        }
    }


    @Override
    public <J> Children eq(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val) {
        return addCondition(condition, column, SqlKeyword.EQ, val);
    }

    @Override
    public <J> Children ne(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val) {
        return addCondition(condition, column, SqlKeyword.NE, val);
    }

    @Override
    public <J> Children gt(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val) {
        return addCondition(condition, column, SqlKeyword.GT, val);
    }

    @Override
    public <J> Children ge(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val) {
        return addCondition(condition, column, SqlKeyword.GE, val);
    }

    @Override
    public <J> Children lt(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val) {
        return addCondition(condition, column, SqlKeyword.LT, val);
    }

    @Override
    public <J> Children le(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val) {
        return addCondition(condition, column, SqlKeyword.LE, val);
    }

    @Override
    public <J, J2> Children between(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val1, SFunction<J2, Object> val2) {
        return maybeDo(condition, () -> super.appendSqlSegments(super.columnToSqlSegment(column), BETWEEN, () -> columnToString(val1, true, false), AND, () -> columnToString(val2, true, false)));
    }

    @Override
    public <J, J2> Children notBetween(boolean condition, SFunction<T, Object> column, SFunction<J, Object> val1, SFunction<J2, Object> val2) {
        return maybeDo(condition, () -> super.appendSqlSegments(super.columnToSqlSegment(column), NOT_BETWEEN, () -> columnToString(val1, true, false), AND, () -> columnToString(val2, true, false)));
    }

    /**
     * 转换查询Wrapper 会把 查询条件，group，order by，having转换来
     * 注意该方法无法给 多个入参添加别名，例如 orderByDesc("id","id2")
     * 这种别名就会添加错误
     *
     * @param queryWrapper
     * @return
     */
    public Children changeQueryWrapper(AbstractWrapper queryWrapper) {
        MergeSegments mergeSegments = queryWrapper.getExpression();

        String id = IdUtil.getSimpleUUID();
        int condtionCount = readWrapperInfo(getAlias(), mergeSegments, id, true);
        if (condtionCount > 0) {
            appendSqlSegments(APPLY, queryWrapper);
        }

        getParamNameValuePairs().put(id, queryWrapper.getParamNameValuePairs());

        return typedThis;
    }

    /**
     * 普通查询条件
     *
     * @param condition  是否执行
     * @param column     属性
     * @param sqlKeyword SQL 关键词
     * @param val        条件值
     */
    protected <J> Children addCondition(boolean condition, SFunction<T, Object> column, SqlKeyword sqlKeyword, SFunction<J, Object> val) {
        return maybeDo(condition, () -> super.appendSqlSegments(super.columnToSqlSegment(column), sqlKeyword, () -> columnToString(val, true, false)));
    }

    protected String getAlias() {
        return getAlias(null);
    }

    /**
     * 获取 增加别名后的字段
     *
     * @param fieldName 字段
     * @return 别名 + 字段
     */
    protected String getAliasAndField(String fieldName) {

        String alias = getAlias();

        return alias + StringPool.DOT + fieldName;
    }

    protected String getAliasAndField(Class<?> fieldClass, String fieldName) {
        String alias = getAlias(fieldClass);
        return alias + StringPool.DOT + fieldName;
    }

    /**
     * 使用匿名函数方式更方便
     *
     * @param consumer 消费函数
     * @return Children
     */
    public Children selectAs(Consumer<ColumnsBuilder<T>> consumer) {
        ColumnsBuilder<T> columnsBuilder = new ColumnsBuilder<>();
        columnsBuilder.setTableName(getAlias());
        // 执行用户自定义定义
        consumer.accept(columnsBuilder);
        // 进行构建
        selectAs(getSelectColumn(columnsBuilder.getColumnsBuilderList()));
        return typedThis;
    }

    /**
     * 查询并设置别名
     *
     * @param columns 列
     * @return 子
     */
    protected Children selectAs(List<String> columns) {
        if (CollectionUtils.isNotEmpty(columns)) {
            this.sqlSelect.addAll(Lists.changeList(columns, SharedString::new));
        }
        return typedThis;
    }

    public Children selectAs(SFunction<T, ?> column, String alias) {
        return this.selectAs(getSelectColumn(Collections.singletonList(new As<>(column, alias))));
    }

    public <J> Children selectAs(SFunction<T, ?> column, SFunction<J, ?> alias) {
        return this.selectAs(getSelectColumn(Collections.singletonList(new As<>(column, alias))));
    }

    /**
     * 获取查询列
     *
     * @param columns 列集合
     * @return List<String>
     */
    protected List<String> getSelectColumn(List<As<T>> columns) {
        return columns.stream()
                .map(this::processColumnAs)
                .collect(Collectors.toList());
    }

    /**
     * 处理单个查询字段和别名
     */
    private String processColumnAs(As<T> as) {
        String column = as.getColumnStr().toString();

        if (as.getColumn() != null) {
            column = processColumnWithLambda(as);
        } else {
            column = processColumnWithoutLambda(as);
        }

        return appendAliasIfNeeded(column, as.getAlias());
    }

    /**
     * 处理带Lambda表达式的列
     */
    private String processColumnWithLambda(As<T> as) {
        if (StringUtils.isNotBlank(as.getAlias())) {
            String columnNotAlias = columnToStringNoAlias(as.getColumn(), false);
            String column = getAliasAndField(columnNotAlias);
            // 设置字段映射
            setFieldMappingList(
                    StringUtils.isNotBlank(as.getFieldName()) ? as.getFieldName() : as.getAlias(),
                    as.getAlias()
            );
            return column;
        }
        return columnToString(as.getColumn(), true, true);
    }

    /**
     * 处理不带Lambda表达式的列
     */
    private String processColumnWithoutLambda(As<T> as) {
        String column = as.isIfQuotes() ? StringUtils.quotaMark(as.getColumnStr().toString())
                : as.getColumnStr().toString();
        setFieldMappingList(
                StringUtils.isNotBlank(as.getFieldName()) ? as.getFieldName() : as.getAlias(),
                as.getAlias()
        );
        return column;
    }

    /**
     * 如果需要，添加别名
     */
    private String appendAliasIfNeeded(String column, String alias) {
        if (StringUtils.isNotBlank(alias)) {
            return String.format(SqlExcerpt.COLUMNS_AS.getSql(), column, alias);
        }
        return column;
    }

    /**
     * 获取 SerializedLambda 对应的列信息，从 lambda 表达式中推测实体类
     * <p>
     * 如果获取不到列信息，那么本次条件组装将会失败
     *
     * @param lambda     lambda 表达式
     * @param onlyColumn 如果是，结果: "name", 如果否： "name" as "name"
     * @param saveField  是否保存到字段列表
     * @return 列
     * @throws MybatisPlusException 获取不到列信息时抛出异常
     * @see SerializedLambda#getImplMethodName()
     */
    protected String getColumn(LambdaMeta lambda, boolean onlyColumn, boolean saveField) {
        // 参数校验
        Assert.notNull(lambda, "lambda cannot be null");

        // 获取字段名和类
        String fieldName = PropertyNamer.methodToProperty(lambda.getImplMethodName());
        Class<?> entityClass = getTableClass(lambda.getInstantiatedClass());

        // 初始化或获取列缓存
        initColumnMapIfNeeded(entityClass);
        Map<String, ColumnCache> classColumnMap = getColumnMap(entityClass);

        // 获取列缓存
        String cacheKey = LambdaUtils.formatKey(fieldName);
        ColumnCache columnCache = classColumnMap.get(cacheKey);
        Assert.notNull(columnCache, "can not find lambda cache for property [%s] of entity [%s]",
                fieldName, entityClass.getName());

        // 获取列信息
        String column = onlyColumn ? columnCache.getColumn() : columnCache.getColumnSelect();

        // 保存字段映射
        if (saveField) {
            setFieldMappingList(fieldName, column);
        }

        return column;
    }

    /**
     * 初始化列映射缓存
     */
    private void initColumnMapIfNeeded(Class<?> entityClass) {
        columnMap.computeIfAbsent(entityClass, key -> {
            Map<String, ColumnCache> cache = LambdaUtils.getColumnMap(entityClass);
            Assert.notNull(cache, "can not find lambda cache for entity [%s]", entityClass.getName());
            return cache;
        });
    }

    /**
     * 获取列映射
     */
    private Map<String, ColumnCache> getColumnMap(Class<?> entityClass) {
        return columnMap.get(entityClass);
    }

    /**
     * 获取表对应的class  主要用于 vo dto这种对应实体
     *
     * @param clz 类
     * @return 对应后的类
     */
    protected Class<?> getTableClass(Class<?> clz) {
        return ClassUtils.getTableClass(clz);
    }

    /**
     * 获取实体并关联的
     *
     * @return Class<?>
     */
    protected Class<?> getEntityOrMasterClass() {
        Class<T> aClass = getEntityClass();

        if (null != aClass) {
            return getTableClass(aClass);
        }

        return null;
    }

    @Override
    protected void initNeed() {
        super.initNeed();
        final Class<?> entityClass = getEntityOrMasterClass();
        if (entityClass != null) {
            columnMap.put(entityClass, LambdaUtils.getColumnMap(entityClass));
            initColumnMap = true;
        }
    }


}
