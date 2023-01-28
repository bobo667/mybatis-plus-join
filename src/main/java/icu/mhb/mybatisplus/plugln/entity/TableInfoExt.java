package icu.mhb.mybatisplus.plugln.entity;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.COMMA;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.EMPTY;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.EQUALS;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.NEWLINE;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.NULL;
import static java.util.stream.Collectors.joining;

import java.util.Objects;
import java.util.function.Predicate;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

import lombok.Getter;

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
    public String chooseSelect(Predicate<TableFieldInfo> predicate, String alias) {
        String sqlSelect = StringUtils.isNotBlank(tableInfo.getKeyColumn()) ? TableFieldInfoExt.getAliasColumn(tableInfo.getKeySqlSelect(), alias) : "";
        String fieldsSqlSelect = tableInfo.getFieldList().stream().filter(predicate)
                .map(TableFieldInfo::getSqlSelect).map(i -> TableFieldInfoExt.getAliasColumn(i, alias))
                .collect(joining(COMMA));
        if (StringUtils.isNotBlank(sqlSelect) && StringUtils.isNotBlank(fieldsSqlSelect)) {
            return sqlSelect + COMMA + fieldsSqlSelect;
        } else if (StringUtils.isNotBlank(fieldsSqlSelect)) {
            return fieldsSqlSelect;
        }
        return sqlSelect;
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
                        return !(tableInfo.isLogicDelete() && i.isLogicDelete());
                    }
                    return true;
                })
                .map(TableFieldInfoExt::new)
                .map(i -> i.getSqlWhere(newPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));

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
        if (tableInfo.isLogicDelete()) {
            TableFieldInfo field = tableInfo.getFieldList().stream().filter(TableFieldInfo::isLogicDelete).findFirst()
                    .orElseThrow(() -> ExceptionUtils.mpe("can't find the logicFiled from table {%s}", tableInfo.getTableName()));
            String logicDeleteSql = formatLogicDeleteSql(field, isWhere, alias);
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

    /**
     * format logic delete SQL, can be overrided by subclass
     * github #1386
     *
     * @param isWhere true: logicDeleteValue, false: logicNotDeleteValue
     * @return sql
     */
    private String formatLogicDeleteSql(TableFieldInfo fieldInfo, boolean isWhere, String alias) {
        final String value = isWhere ? fieldInfo.getLogicNotDeleteValue() : fieldInfo.getLogicDeleteValue();
        if (isWhere) {
            if (NULL.equalsIgnoreCase(value)) {
                return TableFieldInfoExt.getAliasColumn(fieldInfo.getColumn(), alias) + " IS NULL";
            } else {
                return TableFieldInfoExt.getAliasColumn(fieldInfo.getColumn(), alias) + EQUALS + String.format(fieldInfo.isCharSequence() ? "'%s'" : "%s", value);
            }
        }
        final String targetStr = TableFieldInfoExt.getAliasColumn(fieldInfo.getColumn(), alias) + EQUALS;
        if (NULL.equalsIgnoreCase(value)) {
            return targetStr + NULL;
        } else {
            return targetStr + String.format(fieldInfo.isCharSequence() ? "'%s'" : "%s", value);
        }
    }


}
