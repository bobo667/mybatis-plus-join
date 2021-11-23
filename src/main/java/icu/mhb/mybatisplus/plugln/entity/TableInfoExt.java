package icu.mhb.mybatisplus.plugln.entity;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import lombok.Data;
import lombok.Getter;

import java.util.Objects;

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
                .map(i -> i.getSqlWhere(newPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));

        if (!withId || StringUtils.isBlank(tableInfo.getKeyProperty())) {
            return filedSqlScript;
        }

        String newKeyProperty = newPrefix + tableInfo.getKeyProperty();
        String keySqlScript = tableInfo.getKeyColumn() + EQUALS + SqlScriptUtils.safeParam(newKeyProperty);
        return SqlScriptUtils.convertIf(keySqlScript, String.format("%s != null", newKeyProperty), false)
                + NEWLINE + filedSqlScript;
    }


}
