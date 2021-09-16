package com.github.mybatisplus.plugln.injector.methods;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.github.mybatisplus.plugln.enums.JoinSqlMethod;
import com.github.mybatisplus.plugln.constant.JoinConstant;
import com.github.mybatisplus.plugln.injector.JoinAbstractMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author mahuibo
 * @Title: JoinSelectCount
 * @time 8/27/21 3:14 PM
 */
public class JoinSelectCount extends JoinAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        JoinSqlMethod sqlMethod = JoinSqlMethod.JOIN_SELECT_COUNT;

        // 转换一下
        modelClass = getTableClass(modelClass);

        String sql = String.format(sqlMethod.getSql(), sqlFirst(), getJoinTableName(tableInfo), JoinConstant.JOIN_SQL_NAME,
                                   sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, Integer.class);
    }

}
