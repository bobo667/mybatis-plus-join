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
 * @Title: JoinSelectOne
 * @time 9/6/21 4:33 PM
 */
public class JoinSelectOne extends JoinAbstractMethod {

    public JoinSelectOne(String methodName) {
        super(methodName);
    }

    public JoinSelectOne() {
        super(JoinSqlMethod.JOIN_SELECT_ONE.name());
    }

    @Override
    @SuppressWarnings("all")
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        JoinSqlMethod sqlMethod = JoinSqlMethod.JOIN_SELECT_ONE;

        setTableInfo(tableInfo);

        String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true), getJoinTableName(), JoinConstant.JOIN_SQL_NAME,
                                   sqlWhereAliasEntityWrapper(true), sqlComment());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, JoinDefaultResultType.class);
    }

}
