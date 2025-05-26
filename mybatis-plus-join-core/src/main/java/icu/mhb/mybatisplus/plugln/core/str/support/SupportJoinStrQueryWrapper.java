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
     * 主表别名
     */
    protected String masterTableAlias;

    /**
     * join sql和别名映射
     */
    protected Map<String, SharedString> joinSqlMapping;

    /**
     * 是否默认查询全部
     */
    protected boolean notDefaultSelectAll = false;

    /**
     * 是否使用distinct
     */
    protected boolean hasDistinct = false;

    /**
     * 自定义别名映射
     */
    protected Map<String, String> customAliasMap;

    @Override
    protected void initNeed() {
        super.initNeed();
        this.joinSqlMapping = new HashMap<>();
        this.customAliasMap = new HashMap<>();
    }

    /**
     * 查询所有字段
     */
    public Children selectAll() {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityOrMasterClass());
        if (tableInfo != null) {
            TableInfoExt infoExt = new TableInfoExt(tableInfo);
            this.sqlSelect.addAll(Lists.changeList(infoExt.chooseSelect(i -> true, getAlias()), SharedString::new));
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
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        if (tableInfo == null) {
            return null;
        }
        return tableInfo.getFieldList().stream()
                .filter(i -> i.getProperty().equals(fieldName))
                .findFirst()
                .orElse(null);
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
     * 构建Join SQL
     */
    protected void buildJoinSql(String joinTableField, String masterTableField, SqlExcerpt joinType) {
        SharedString sharedString = SharedString.emptyString();

        // 获取当前类的表信息
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityOrMasterClass());
        String tableName = tableInfo != null ? tableInfo.getTableName() : "";

        // 构建Join SQL
        StringBuilder sb = new StringBuilder(String.format(
                joinType.getSql(),
                tableName,
                getAlias(),
                getAlias(),
                joinTableField,
                masterTableAlias,
                masterTableField
        ));

        // 处理逻辑删除
        if (tableInfo != null) {
            TableInfoExt infoExt = new TableInfoExt(tableInfo);
            String logicDeleteSql = infoExt.getLogicDeleteSql(true, true, getAlias());
            if (StringUtils.isNotBlank(logicDeleteSql)) {
                sb.append(SPACE).append(logicDeleteSql);
            }
        }

        sharedString.setStringValue(sb.toString());
        String alias = getAlias();
        joinSqlMapping.put(alias, sharedString);
    }

    /**
     * 构建Join SQL - 带表名和别名
     */
    protected void buildJoinSql(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType) {
        SharedString sharedString = SharedString.emptyString();

        // 构建Join SQL
        StringBuilder sb = new StringBuilder(String.format(
                joinType.getSql(),
                joinTable,
                alias,
                alias,
                joinTableField,
                masterTableAlias,
                masterTableField
        ));

        // 处理逻辑删除
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityOrMasterClass());
        if (tableInfo != null) {
            TableInfoExt infoExt = new TableInfoExt(tableInfo);
            String logicDeleteSql = infoExt.getLogicDeleteSql(true, true, alias);
            if (StringUtils.isNotBlank(logicDeleteSql)) {
                sb.append(SPACE).append(logicDeleteSql);
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
        if (StringUtils.isNotBlank(column) && !column.contains(DOT)) {
            return alias + DOT + column;
        }
        return column;
    }

    /**
     * 处理查询字段前缀
     * 处理子查询中的select字段，为没有前缀的字段添加表别名前缀
     *
     * @param alias 表别名
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
                                SqlExcerpt joinType, Consumer<Children> consumer) {
        buildJoinSql(joinTable, joinTableField, masterTableField, alias, joinType);
        
        if (consumer != null) {
            Children children = instance();
            children.setAlias(alias);
            children.paramNameSeq.set(this.paramNameSeq.intValue());
            
            consumer.accept(children);
            
            // 处理select字段，为没有前缀的字段添加表别名前缀
            handleSelectPrefix(alias, children);
            
            // 添加处理后的select字段到主查询
            if (!children.sqlSelect.isEmpty()) {
                this.sqlSelect.addAll(children.sqlSelect);
            }
            
            // 处理条件
            String conditionSql = children.getCustomSqlSegment();
            if (StringUtils.isNotBlank(conditionSql)) {
                conditionSql = conditionSql.replaceFirst("WHERE", SPACE);
                this.paramNameSeq.set(children.paramNameSeq.intValue());
                this.paramNameValuePairs.putAll(children.paramNameValuePairs);
                
                SharedString joinSql = joinSqlMapping.get(alias);
                joinSql.setStringValue(joinSql.getStringValue() + SPACE + AND + conditionSql);
                
                joinSqlMapping.put(alias, joinSql);
            }
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

}
