package icu.mhb.mybatisplus.plugln.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import icu.mhb.mybatisplus.plugln.enums.JoinSqlMethod;
import icu.mhb.mybatisplus.plugln.injector.JoinAbstractMethod;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Map;

/**
 * @author mahuibo
 * @Title: JoinSelectList
 * @time 8/25/21 10:47 AM
 */
public class JoinSelectList extends JoinAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        JoinSqlMethod sqlMethod = JoinSqlMethod.JOIN_SELECT_LIST;
        String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true), getJoinTableName(tableInfo), JoinConstant.JOIN_SQL_NAME,
                                   sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, Map.class);
    }

}
