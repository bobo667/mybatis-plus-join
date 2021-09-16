package com.github.mybatisplus.plugln.injector.methods;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.github.mybatisplus.plugln.core.JoinLambdaWrapper;
import com.github.mybatisplus.plugln.enums.JoinSqlMethod;
import com.github.mybatisplus.plugln.constant.JoinConstant;
import com.github.mybatisplus.plugln.injector.JoinAbstractMethod;
import com.github.mybatisplus.plugln.tookit.ClassUtils;
import com.github.mybatisplus.plugln.tookit.IdUtil;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Map;

/**
 * @author mahuibo
 * @Title: JoinSelectOne
 * @time 9/6/21 4:33 PM
 */
@SuppressWarnings("all")
public class JoinSelectOne extends JoinAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        JoinSqlMethod sqlMethod = JoinSqlMethod.JOIN_SELECT_ONE;

        String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true), getJoinTableName(tableInfo), JoinConstant.JOIN_SQL_NAME,
                                   sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, Map.class);
    }

}
