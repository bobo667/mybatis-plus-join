package icu.mhb.mybatisplus.plugln.entity;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import icu.mhb.mybatisplus.plugln.exception.MybatisPlusJoinException;
import icu.mhb.mybatisplus.plugln.tookit.Lists;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;
import static java.util.stream.Collectors.joining;

/**
 * tableInfo的扩展类
 *
 * @author mahuibo
 * @Title: TableFieldInfoExt
 * @time 11/23/21 9:21 PM
 */
@Getter
public class TableInfoExt {

    private TableInfo tableInfo;

    public TableInfoExt(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    /**
     * 获取需要进行查询的 select sql 片段
     *
     * @param predicate 过滤条件
     * @return sql 片段
     */
    public List<String> chooseSelect(Predicate<TableFieldInfo> predicate, String alias) {
        String sqlSelect = tableInfo.havePK() ? TableFieldInfoExt.getAliasColumn(tableInfo.getKeySqlSelect(), alias) : "";
        List<String> fieldsSqlSelect = tableInfo.getFieldList().stream().filter(predicate)
                .map(TableFieldInfo::getSqlSelect).map(i -> TableFieldInfoExt.getAliasColumn(i, alias))
                .collect(Collectors.toList());
        if (StringUtils.isNotBlank(sqlSelect) && CollectionUtils.isNotEmpty(fieldsSqlSelect)) {
            fieldsSqlSelect.add(sqlSelect);
        }
        if (CollectionUtils.isNotEmpty(fieldsSqlSelect)) {
            return fieldsSqlSelect;
        }
        return StringUtils.isNotBlank(sqlSelect) ? Lists.newArrayList(sqlSelect) : Lists.newArrayList();
    }



    /**
     * 获取所有的查询的 sql 片段
     *
     * @param ignoreLogicDelFiled 是否过滤掉逻辑删除字段
     * @param withId              是否包含 id 项
     * @param prefix              前缀
     * @return sql 脚本片段
     */
    public String getAllSqlWhere(boolean ignoreLogicDelFiled, boolean withId, final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        String filedSqlScript = tableInfo.getFieldList().stream()
                .filter(i -> {
                    if (ignoreLogicDelFiled) {
                        return !(tableInfo.isWithLogicDelete() && i.isLogicDelete());
                    }
                    return true;
                })
                .map(TableFieldInfoExt::new)
                .map(i -> i.getSqlWhere(newPrefix)).filter(Objects::nonNull).collect(joining(SPACE));

        if (!withId || StringUtils.isBlank(tableInfo.getKeyProperty())) {
            return filedSqlScript;
        }

        String newKeyProperty = newPrefix + tableInfo.getKeyProperty();
        String keySqlScript = TableFieldInfoExt.getAliasColumn(tableInfo.getKeyColumn(), null) + EQUALS + SqlScriptUtils.safeParam(newKeyProperty);
        return SqlScriptUtils.convertIf(keySqlScript, String.format("%s != null", newKeyProperty), false)
                + NEWLINE + filedSqlScript;
    }


    /**
     * 获取逻辑删除字段的 sql 脚本
     *
     * @param startWithAnd 是否以 and 开头
     * @param isWhere      是否需要的是逻辑删除值
     * @return sql 脚本
     */
    public String getLogicDeleteSql(boolean startWithAnd, boolean isWhere, String alias) {
        if (tableInfo.isWithLogicDelete()) {
            String logicDeleteSql = formatLogicDeleteSql(isWhere, alias);
            if (startWithAnd) {
                logicDeleteSql = " AND " + logicDeleteSql;
            }
            return logicDeleteSql;
        }
        return EMPTY;
    }

    public String getLogicDeleteSql(boolean startWithAnd, boolean isWhere) {
        return getLogicDeleteSql(startWithAnd, isWhere, null);
    }

    public static TableInfoExt get(Class<?> clz) {
        TableInfo info = TableInfoHelper.getTableInfo(clz);
        return new TableInfoExt(info);
    }

    /**
     * 根据属性名查询字段
     *
     * @param property 属性名
     * @return 对应的字段名
     */
    public String getColumByProperty(String property) {
        if (tableInfo.havePK() && tableInfo.getKeyProperty().equals(property)) {
            return tableInfo.getKeyColumn();
        }
        return tableInfo.getFieldList()
                .stream().filter(i -> i.getProperty().equals(property))
                .map(TableFieldInfo::getColumn)
                .findFirst().orElseGet(() -> EMPTY);
    }

    /**
     * format logic delete SQL, can be overrided by subclass
     * github #1386
     *
     * @param isWhere true: logicDeleteValue, false: logicNotDeleteValue
     * @return sql
     */
    private String formatLogicDeleteSql(boolean isWhere, String alias) {
        final String value = isWhere ? tableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue() : tableInfo.getLogicDeleteFieldInfo().getLogicDeleteValue();
        if (isWhere) {
            if (NULL.equalsIgnoreCase(value)) {
                return TableFieldInfoExt.getAliasColumn(tableInfo.getLogicDeleteFieldInfo().getColumn(), alias) + " IS NULL";
            } else {
                return TableFieldInfoExt.getAliasColumn(tableInfo.getLogicDeleteFieldInfo().getColumn(), alias) + EQUALS + String.format(tableInfo.getLogicDeleteFieldInfo().isCharSequence() ? "'%s'" : "%s", value);
            }
        }
        final String targetStr = TableFieldInfoExt.getAliasColumn(tableInfo.getLogicDeleteFieldInfo().getColumn(), alias) + EQUALS;
        if (NULL.equalsIgnoreCase(value)) {
            return targetStr + NULL;
        } else {
            return targetStr + String.format(tableInfo.getLogicDeleteFieldInfo().isCharSequence() ? "'%s'" : "%s", value);
        }
    }


}
