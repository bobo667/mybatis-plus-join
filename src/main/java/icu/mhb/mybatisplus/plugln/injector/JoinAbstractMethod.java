package icu.mhb.mybatisplus.plugln.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.entity.TableInfoExt;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;

/**
 * @author mahuibo
 * @Title: JoinAbstractMethod
 * @time 8/27/21 3:53 PM
 */
public abstract class JoinAbstractMethod extends AbstractMethod {

    private TableInfoExt table;

    protected JoinAbstractMethod(String methodName) {
        super(methodName);
    }

    protected TableInfoExt getTableInfo() {
        return this.table;
    }

    protected void setTableInfo(TableInfo tableInfo) {
        this.table = new TableInfoExt(tableInfo);
    }


    /**
     * 获取join 别名后的表名
     *
     * @return 表名
     */
    protected String getJoinTableName() {
        return String.format(SqlExcerpt.TABLE_AS.getSql(), table.getTableInfo().getTableName(), JoinConstant.TABLE_ALIAS_NAME);
    }

    /**
     * EntityWrapper方式获取select where
     *
     * @param newLine 是否提到下一行
     * @param table   表信息
     * @return String
     */
    protected String sqlWhereAliasEntityWrapper(boolean newLine) {
        if (table.getTableInfo().isWithLogicDelete()) {
            String sqlScript = table.getAllSqlWhere(true, true, WRAPPER_ENTITY_DOT);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER_ENTITY),
                    true);
            sqlScript += SqlScriptUtils.convertIf((NEWLINE + table.getLogicDeleteSql(true, true) + NEWLINE), "ew.masterLogicDelete",
                    true);
            String normalSqlScript = SqlScriptUtils.convertIf(String.format("AND ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_NONEMPTYOFNORMAL), true);
            normalSqlScript += NEWLINE;
            normalSqlScript += SqlScriptUtils.convertIf(String.format(" ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_EMPTYOFNORMAL), true);
            sqlScript += normalSqlScript;
            sqlScript = SqlScriptUtils.convertChoose(String.format("%s != null", WRAPPER), sqlScript,
                    table.getTableInfo().getLogicDeleteSql(false, true));
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
            return newLine ? NEWLINE + sqlScript : sqlScript;
        } else {
            String sqlScript = table.getAllSqlWhere(false, true, WRAPPER_ENTITY_DOT);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER_ENTITY), true);
            sqlScript += NEWLINE;
            sqlScript += SqlScriptUtils.convertIf(String.format(SqlScriptUtils.convertIf(" AND", String.format("%s and %s", WRAPPER_NONEMPTYOFENTITY, WRAPPER_NONEMPTYOFNORMAL), false) + " ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_NONEMPTYOFWHERE), true);
            sqlScript = SqlScriptUtils.convertWhere(sqlScript) + NEWLINE;
            sqlScript += SqlScriptUtils.convertIf(String.format(" ${%s}", WRAPPER_SQLSEGMENT),
                    String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                            WRAPPER_EMPTYOFWHERE), true);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER), true);
            return newLine ? NEWLINE + sqlScript : sqlScript;
        }
    }

    /**
     * 获取表类信息
     *
     * @param clz 类信息
     * @return 转换后的class
     */
    protected Class<?> getTableClass(Class<?> clz) {
        return ClassUtils.getTableClass(clz);
    }


}
