package icu.mhb.mybatisplus.plugln.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.enums.JoinSqlMethod;
import icu.mhb.mybatisplus.plugln.injector.JoinAbstractMethod;
import icu.mhb.mybatisplus.plugln.injector.JoinDefaultResultType;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author mahuibo
 * @Title: JoinSelectList
 * @time 8/25/21 10:47 AM
 */
public class JoinSelectList extends JoinAbstractMethod {

    public JoinSelectList(String methodName) {
        super(methodName);
    }

    public JoinSelectList() {
        super(JoinSqlMethod.JOIN_SELECT_LIST.name());
    }

    @Override
    @SuppressWarnings("all")
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        JoinSqlMethod sqlMethod = JoinSqlMethod.JOIN_SELECT_LIST;

        setTableInfo(tableInfo);

        String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true), getJoinTableName(), JoinConstant.JOIN_SQL_NAME,
                                   sqlWhereAliasEntityWrapper(true), sqlComment());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, JoinDefaultResultType.class);
    }

}
