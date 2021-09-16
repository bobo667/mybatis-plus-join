package com.github.mybatisplus.plugln.core.support;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.github.mybatisplus.plugln.annotations.TableAlias;
import com.github.mybatisplus.plugln.entity.ColumnsBuilder;
import com.github.mybatisplus.plugln.enums.SqlExcerpt;
import com.github.mybatisplus.plugln.tookit.ClassUtils;
import com.github.mybatisplus.plugln.tookit.TableAliasCache;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.*;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.COMMA;
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
public abstract class SupportJoinLambdaWrapper<T, Children extends SupportJoinLambdaWrapper<T, Children>>
        extends AbstractWrapper<T, SFunction<T, ?>, Children> {

    /**
     * 查询字段
     */
    protected SharedString sqlSelect = SharedString.emptyString();

    private Map<Class<?>, Map<String, ColumnCache>> columnMap = new HashMap<>();

    private boolean initColumnMap = false;

    @SuppressWarnings("unchecked")
    @Override
    protected String columnsToString(SFunction<T, ?>... columns) {
        return columnsToString(true, columns);
    }

    @SuppressWarnings("unchecked")
    protected String columnsToString(boolean onlyColumn, SFunction<T, ?>... columns) {
        return Arrays.stream(columns).map(i -> columnToString(i, onlyColumn)).collect(joining(StringPool.COMMA));
    }

    @Override
    protected String columnToString(SFunction<T, ?> column) {
        return columnToString(column, true);
    }

    protected String columnToString(SFunction<T, ?> column, boolean onlyColumn) {
        String columnToString = getColumn(LambdaUtils.resolve(column), onlyColumn);
        if (StringUtils.isNotBlank(columnToString)) {
            return getAliasAndField(columnToString);
        }
        return columnToString;
    }

    /**
     * 获取当前类的所有查询字段
     */
    public final Children selectAll() {

        Class<?> clz = getEntityOrMasterClass();

        Assert.notNull(clz, "Can't get the current parser class");

        TableInfo tableInfo = TableInfoHelper.getTableInfo(clz);
        String keySqlSelect = tableInfo.getKeySqlSelect();
        String sqlSelect = tableInfo.getFieldList().stream().filter(TableFieldInfo::isSelect)
                .map(TableFieldInfo::getSqlSelect)
                .map(this::getAliasAndField)
                .collect(joining(COMMA));

        // 不为空代表有主键
        if (StringUtils.isNotBlank(keySqlSelect)) {
            sqlSelect += COMMA + getAliasAndField(keySqlSelect);
        }

        this.sqlSelect.setStringValue(sqlSelect);
        return typedThis;
    }

    /**
     * 获取当前类所有查询字段 并增加排除
     *
     * @param excludeColumn 需要排除的字段
     * @return Children
     */
    public final Children selectAll(Collection<SFunction<T, ?>> excludeColumn) {

        Class<?> clz = getEntityOrMasterClass();

        Assert.notNull(clz, "Can't get the current parser class");

        // 需要排除的字段列表
        List<String> excludeList = excludeColumn.stream().map(i -> columnToString(i, true)).collect(Collectors.toList());

        TableInfo tableInfo = TableInfoHelper.getTableInfo(clz);
        String sqlSelect = tableInfo.getFieldList().stream().filter(TableFieldInfo::isSelect)
                .map(TableFieldInfo::getSqlSelect)
                .map(this::getAliasAndField)
                .filter(str -> !excludeList.contains(str))
                .collect(joining(COMMA));
        this.sqlSelect.setStringValue(sqlSelect);
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

        Assert.notNull(clz, "Can't get the current parser class");

        clz = getTableClass(clz);

        final Class<?> finalClz = clz;

        return TableAliasCache.getOrSet(clz, (key) -> {
            String alias;

            TableAlias tableAlias = finalClz.getAnnotation(TableAlias.class);
            // 如果表名为空
            if (tableAlias == null) {
                TableInfo tableInfo = TableInfoHelper.getTableInfo(finalClz);
                alias = tableInfo.getTableName();
            } else {
                alias = tableAlias.value();
            }
            return alias;
        });
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

    /**
     * 查询并设置别名
     *
     * @param columns 列
     * @return 子
     */
    public final Children selectAs(List<ColumnsBuilder<T>> columns) {
        if (CollectionUtils.isNotEmpty(columns)) {
            List<String> columnsStringList = new ArrayList<>();
            for (ColumnsBuilder<T> columnsBuilder : columns) {
                String column = columnsBuilder.getColumnStr().toString();

                if (columnsBuilder.getColumn() != null) {
                    // 获取序列化后的列明
                    column = columnToString(columnsBuilder.getColumn());
                }

                if (StringUtils.isNotBlank(columnsBuilder.getAlias())) {
                    column = String.format(SqlExcerpt.AS.getSql(), column, columnsBuilder.getAlias());
                }
                columnsStringList.add(column);
            }
            this.sqlSelect.setStringValue(
                    String.join(",", columnsStringList)
            );
        }
        return typedThis;
    }

    public final Children selectAs(SFunction<T, ?> column, String alias) {
        return this.selectAs(Collections.singletonList(new ColumnsBuilder<>(column, alias)));
    }

    /**
     * 获取 SerializedLambda 对应的列信息，从 lambda 表达式中推测实体类
     * <p>
     * 如果获取不到列信息，那么本次条件组装将会失败
     *
     * @param lambda     lambda 表达式
     * @param onlyColumn 如果是，结果: "name", 如果否： "name" as "name"
     * @return 列
     * @throws MybatisPlusException 获取不到列信息时抛出异常
     * @see SerializedLambda#getImplClass()
     * @see SerializedLambda#getImplMethodName()
     */
    protected String getColumn(SerializedLambda lambda, boolean onlyColumn) throws MybatisPlusException {
        String fieldName = PropertyNamer.methodToProperty(lambda.getImplMethodName());
        Class<?> aClass = getTableClass(lambda.getInstantiatedType());
        columnMap.computeIfAbsent(aClass, (key) -> LambdaUtils.getColumnMap(aClass));
        Assert.notNull(columnMap.get(aClass), "can not find lambda cache for this entity [%s]", aClass.getName());
        ColumnCache columnCache = columnMap.get(aClass).get(LambdaUtils.formatKey(fieldName));
        Assert.notNull(columnCache, "can not find lambda cache for this property [%s] of entity [%s]",
                       fieldName, aClass.getName());
        return onlyColumn ? columnCache.getColumn() : columnCache.getColumnSelect();
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
