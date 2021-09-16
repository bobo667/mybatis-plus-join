package com.github.mybatisplus.plugln.injector.methods;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.github.mybatisplus.plugln.enums.JoinSqlMethod;
import com.github.mybatisplus.plugln.constant.JoinConstant;
import com.github.mybatisplus.plugln.injector.JoinAbstractMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Map;

/**
 * @author mahuibo
 * @Title: JoinSelectPage
 * @time 9/7/21 10:56 AM
 */
public class JoinSelectPage extends JoinAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        JoinSqlMethod sqlMethod = JoinSqlMethod.JOIN_SELECT_PAGE;
        String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true),
                                   getJoinTableName(tableInfo), JoinConstant.JOIN_SQL_NAME, sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, Map.class);
    }

}
