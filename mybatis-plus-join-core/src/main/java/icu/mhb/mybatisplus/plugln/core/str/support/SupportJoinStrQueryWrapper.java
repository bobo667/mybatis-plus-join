package icu.mhb.mybatisplus.plugln.core.str.support;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ORDER_BY;
import static com.baomidou.mybatisplus.core.enums.WrapperKeyword.APPLY;
import static icu.mhb.mybatisplus.plugln.constant.StringPool.AND;
import static icu.mhb.mybatisplus.plugln.constant.StringPool.DOT;
import static icu.mhb.mybatisplus.plugln.constant.StringPool.SPACE;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import icu.mhb.mybatisplus.plugln.core.str.func.JoinStrCompareFunc;
import icu.mhb.mybatisplus.plugln.core.str.func.JoinStrFunc;
import icu.mhb.mybatisplus.plugln.core.str.func.JoinStrMethodFunc;
import icu.mhb.mybatisplus.plugln.core.str.util.StrQueryWrapperHelper;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinWrapper;
import icu.mhb.mybatisplus.plugln.entity.FieldMapping;
import icu.mhb.mybatisplus.plugln.entity.TableFieldInfoExt;
import icu.mhb.mybatisplus.plugln.entity.TableInfoExt;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.tookit.IdUtil;
import icu.mhb.mybatisplus.plugln.tookit.Lists;

/**
 * String类型Join查询构造器支持类
 *
 * @author mahuibo
 * @Title: SupportJoinStrQueryWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public abstract class SupportJoinStrQueryWrapper<T, Children extends SupportJoinStrQueryWrapper<T, Children>>
        extends SupportJoinWrapper<T, String, Children> implements Query<Children, T, String>,
        JoinStrCompareFunc<Children>, JoinStrMethodFunc<T, Children>, JoinStrFunc<Children> {

    /**
     * join sql和别名映射
     */
    protected Map<String, SharedString> joinSqlMapping;


    /**
     * 别名映射表
     */
    protected Map<String, String> alias2table = new HashMap<>();


    @Override
    protected void initNeed() {
        super.initNeed();
        this.joinSqlMapping = new HashMap<>();
    }

    /**
     * 查询所有字段
     */
    public Children selectAll() {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityOrMasterClass());
        if (tableInfo != null) {
            TableInfoExt infoExt = new TableInfoExt(tableInfo);
            List<String> columns = infoExt.chooseSelect(i -> true, getAlias());

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

            this.sqlSelect.addAll(Lists.changeList(columns, SharedString::new));
        }
        return typedThis;
    }

    /**
     * 获取别名
     */
    public String getAlias() {
        if (StringUtils.isNotBlank(masterTableAlias)) {
            return masterTableAlias;
        }
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityOrMasterClass());
        if (tableInfo != null) {
            return tableInfo.getTableName();
        }
        return "";
    }

    /**
     * 设置别名
     */
    public void setAlias(String alias) {
        this.masterTableAlias = alias;
    }

    /**
     * 根据字段名获取TableFieldInfo
     */
    protected TableFieldInfo getTableFieldInfoByFieldName(String fieldName, Class<?> clazz) {
        return StrQueryWrapperHelper.getTableFieldInfoByFieldName(fieldName, clazz);
    }

    /**
     * 根据字段名获取TableFieldInfo
     */
    protected TableFieldInfo getTableFieldInfoByColumn(String fieldName, Class<?> clazz) {
        return StrQueryWrapperHelper.getTableFieldInfoByColumn(fieldName, clazz);
    }

    /**
     * 根据字段名获取TableFieldInfo
     */
    protected TableFieldInfo getTableFieldInfoByColumn(String fieldName, String tableName) {
        return StrQueryWrapperHelper.getTableFieldInfoByColumn(fieldName, tableName);
    }

    /**
     * 获取别名和字段的组合
     */
    protected String getAliasAndField(String alias, String field) {
        return alias + DOT + field;
    }

    /**
     * 转换查询Wrapper
     */
    public Children changeQueryWrapper(String alias, AbstractWrapper queryWrapper) {
        MergeSegments mergeSegments = queryWrapper.getExpression();

        String id = IdUtil.getSimpleUUID();
        int count = readWrapperInfo(alias, mergeSegments, id, true);
        if (count > 0) {
            appendSqlSegments(APPLY, queryWrapper);
        }

        getParamNameValuePairs().put(id, queryWrapper.getParamNameValuePairs());

        return typedThis;
    }

    /**
     * 构建Join SQL - 带表名和别名
     */
    protected void buildJoinSql(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType, boolean isLogicDelete) {
        SharedString sharedString = SharedString.emptyString();

        String tableAlias = masterTableAlias;
        String field = masterTableField;
        alias2table.put(alias, joinTable);
        // 大于1 代表用户自定义了别名不需要用主表的
        if (masterTableField.split("\\.").length > 1) {
            tableAlias = masterTableField.split("\\.")[0];
            field = masterTableField.split("\\.")[1];
        }

        // 构建Join SQL
        StringBuilder sb = new StringBuilder(String.format(
                joinType.getSql(),
                joinTable,
                alias,
                alias,
                joinTableField,
                tableAlias,
                field
        ));

        // 处理逻辑删除
        if (isLogicDelete) {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(joinTable);
            if (tableInfo != null) {
                TableInfoExt infoExt = new TableInfoExt(tableInfo);
                String logicDeleteSql = infoExt.getLogicDeleteSql(true, true, alias);
                if (StringUtils.isNotBlank(logicDeleteSql)) {
                    sb.append(SPACE).append(logicDeleteSql);
                }
            }
        }

        sharedString.setStringValue(sb.toString());
        joinSqlMapping.put(alias, sharedString);
    }

    /**
     * 处理字段前缀
     * 如果字段没有包含点号"."，则添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @return 处理后的字段名
     */
    protected String handleColumnPrefix(String alias, String column) {
        return StrQueryWrapperHelper.handleColumnPrefix(alias, column);
    }

    /**
     * 检查并处理字段名
     * 如果字段名不包含点号，则自动添加主表别名前缀
     *
     * @param column 字段名
     * @return 处理后的字段名
     */
    protected String checkAndHandleColumn(String column) {
        return StrQueryWrapperHelper.checkAndHandleColumn(column, masterTableAlias);
    }

    /**
     * 处理查询字段前缀
     * 处理子查询中的select字段，为没有前缀的字段添加表别名前缀
     *
     * @param alias    表别名
     * @param children 子查询
     */
    protected void handleSelectPrefix(String alias, Children children) {
        if (children.sqlSelect != null && !children.sqlSelect.isEmpty()) {
            List<SharedString> prefixedSelects = new java.util.ArrayList<>();

            for (SharedString select : children.sqlSelect) {
                String selectStr = select.getStringValue();

                // 跳过已经处理过的字段或带有AS的字段
                if (selectStr.contains(DOT) || selectStr.toLowerCase().contains(" as ")) {
                    prefixedSelects.add(select);
                    continue;
                }

                // 为没有前缀的字段添加别名前缀
                String prefixedSelect = alias + DOT + selectStr;
                prefixedSelects.add(new SharedString(prefixedSelect));
            }

            // 清空原有的select字段并添加处理后的字段
            children.sqlSelect.clear();
            children.sqlSelect.addAll(prefixedSelects);
        }
    }

    /**
     * 构建Join SQL - 带表名、别名和回调函数
     */
    protected void buildJoinSql(String joinTable, String joinTableField, String masterTableField, String alias,
                                SqlExcerpt joinType, Consumer<Children> consumer, boolean isLogicDelete) {
        buildJoinSql(joinTable, joinTableField, masterTableField, alias, joinType, isLogicDelete);

        alias2table.put(alias, joinTable);

        if (consumer != null) {
            String oldMasterTableAlias = masterTableAlias;
            typedThis.masterTableAlias = alias;
            consumer.accept(typedThis);
            typedThis.masterTableAlias = oldMasterTableAlias;
        }
    }

    /**
     * Join AND 条件
     */
    public Children joinAnd(boolean condition, String alias, String field, Object val, BiConsumer<String, Object> consumer) {
        if (!condition) {
            return typedThis;
        }

        if (!joinSqlMapping.containsKey(alias)) {
            throw Exceptions.mpje("别名[%s]未找到对应的Join语句", alias);
        }

        consumer.accept(field, val);
        return typedThis;
    }

    /**
     * JOIN AND 通用实现
     */
    public Children joinAnd(boolean condition, String alias, Consumer<Children> consumer) {
        if (!condition) {
            return typedThis;
        }

        if (!joinSqlMapping.containsKey(alias)) {
            throw Exceptions.mpje("别名[%s]未找到对应的Join语句", alias);
        }

        Children children = instance();
        children.alias2table = this.alias2table;
        children.paramNameSeq.set(this.paramNameSeq.intValue());

        consumer.accept(children);

        String conditionSql = children.getCustomSqlSegment();
        if (StringUtils.isNotBlank(conditionSql)) {
            conditionSql = conditionSql.replaceFirst("WHERE", SPACE);
            this.paramNameSeq.set(children.paramNameSeq.intValue());
            this.paramNameValuePairs.putAll(children.paramNameValuePairs);

            SharedString joinSql = joinSqlMapping.get(alias);
            joinSql.setStringValue(joinSql.getStringValue() + SPACE + AND + conditionSql);

            joinSqlMapping.put(alias, joinSql);
        }

        return typedThis;
    }

    /**
     * 添加Distinct
     */
    public Children distinct() {
        this.hasDistinct = true;
        return typedThis;
    }

    /**
     * 不默认查询全部
     */
    public Children notDefaultSelectAll() {
        this.notDefaultSelectAll = true;
        return typedThis;
    }

    @Override
    protected String columnToString(String column) {
        if (customAliasMap.containsKey(column)) {
            return customAliasMap.get(column);
        }
        return column;
    }

    @Override
    public String getConditionR(Class<?> entityClass, Field field) {
        return IdUtil.getSimpleUUID();
    }

    /**
     * 手写SQL排序
     */
    public Children orderBySql(boolean condition, String sql) {
        return maybeDo(condition, () -> appendSqlSegments(ORDER_BY, columnToSqlSegment(sql)));
    }

    @Override
    protected void setFieldMappingList(String fieldName, String columns) {
        setFieldMappingList(fieldName, columns, null);
    }

    /**
     * 设置字段映射，带有类参数
     *
     * @param fieldName 字段名
     * @param columns   列名
     * @param clz       类
     */
    protected void setFieldMappingList(String fieldName, String columns, Class<?> clz) {
        FieldMapping fieldMapping = StrQueryWrapperHelper.createFieldMapping(
            fieldName, columns, clz, alias2table, masterTableAlias);
        fieldMappingList.add(fieldMapping);
    }

    /**
     * 构建字段映射列表 - 用于oneToOne和manyToMany
     *
     * @param tableNameOrAlias 表名或别名
     * @param columns          字段列表
     * @return 字段映射列表
     */
    protected List<FieldMapping> buildFieldMappingList(String tableNameOrAlias, String... columns) {
        return buildFieldMappingList(tableNameOrAlias, false, columns);
    }

    /**
     * 构建字段映射列表 - 用于oneToOne和manyToMany
     *
     * @param tableNameOrAlias 表名或别名
     * @param autoAlias        是否自动生成别名
     * @param columns          字段列表
     * @return 字段映射列表
     */
    protected List<FieldMapping> buildFieldMappingList(String tableNameOrAlias, boolean autoAlias, String... columns) {
        return StrQueryWrapperHelper.buildFieldMappingList(tableNameOrAlias, autoAlias, alias2table, columns);
    }

    /**
     * 根据字段映射列表添加查询字段
     *
     * @param fieldMappings 字段映射列表
     */
    protected void addSelectByFieldMappings(List<FieldMapping> fieldMappings) {
        StrQueryWrapperHelper.addSelectByFieldMappings(fieldMappings, this.sqlSelect, this.fieldMappingList);
    }

}
