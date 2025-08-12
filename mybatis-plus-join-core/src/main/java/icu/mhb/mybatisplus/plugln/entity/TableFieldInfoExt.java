package icu.mhb.mybatisplus.plugln.entity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import lombok.Data;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.EMPTY;
import static java.util.stream.Collectors.joining;

/**
 * tableField的扩展类
 *
 * @author mahuibo
 * @Title: TableFieldInfoExt
 * @time 11/23/21 9:21 PM
 */
@Data
public class TableFieldInfoExt {

    private TableFieldInfo tableFieldInfo;

    /**
     * 字段名
     */
    private String column;

    /**
     * 属性名
     */
    private String property;

    public TableFieldInfoExt(TableFieldInfo tableFieldInfo) {
        this.tableFieldInfo = tableFieldInfo;
    }


    /**
     * 获取 查询的 sql 片段
     *
     * @param prefix 前缀
     * @return sql 脚本片段
     */
    public String getSqlWhere(final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        // 默认:  AND column=#{prefix + el}
        String sqlScript = " AND " + String.format(tableFieldInfo.getCondition(), getAliasColumn(tableFieldInfo.getColumn(), null), newPrefix + tableFieldInfo.getEl());
        // 查询的时候只判非空
        return convertIf(sqlScript, convertIfProperty(newPrefix, tableFieldInfo.getProperty()), tableFieldInfo.getWhereStrategy());
    }

    /**
     * 获取别名字段拼接
     *
     * @param column 字段
     * @return 添加别名后的字段
     */
    public static String getAliasColumn(String column, String alias) {
        if (StringUtils.isNotBlank(alias)) {
            return alias + "." + column;
        }
        return JoinConstant.TABLE_ALIAS_NAME + "." + column;
    }


    private String convertIfProperty(String prefix, String property) {
        return StringUtils.isNotBlank(prefix) ? prefix.substring(0, prefix.length() - 1) + "['" + property + "']" : property;
    }


    /**
     * 转换成 if 标签的脚本片段
     *
     * @param sqlScript     sql 脚本片段
     * @param property      字段名
     * @param fieldStrategy 验证策略
     * @return if 脚本片段
     */
    private String convertIf(final String sqlScript, final String property, final FieldStrategy fieldStrategy) {
        if (fieldStrategy == FieldStrategy.NEVER) {
            return null;
        }
        if (tableFieldInfo.isPrimitive() || fieldStrategy == FieldStrategy.ALWAYS) {
            return sqlScript;
        }
        if (fieldStrategy == FieldStrategy.NOT_EMPTY && tableFieldInfo.isCharSequence()) {
            return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and %s != ''", property, property),
                                            false);
        }
        return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", property), false);
    }


    /**
     * 获取 ResultMapping
     *
     * @param configuration MybatisConfiguration
     * @return ResultMapping
     */
    public ResultMapping getResultMapping(final Configuration configuration) {
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, getProperty() == null ? tableFieldInfo.getProperty() : getProperty(),
                                                                  StringUtils.getTargetColumn(getColumn() == null ? tableFieldInfo.getColumn() : getColumn()), tableFieldInfo.getPropertyType());
        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        if (tableFieldInfo.getJdbcType() != null && tableFieldInfo.getJdbcType() != JdbcType.UNDEFINED) {
            builder.jdbcType(tableFieldInfo.getJdbcType());
        }
        if (tableFieldInfo.getTypeHandler() != null && tableFieldInfo.getTypeHandler() != UnknownTypeHandler.class) {
            TypeHandler<?> typeHandler = registry.getMappingTypeHandler(tableFieldInfo.getTypeHandler());
            if (typeHandler == null) {
                typeHandler = registry.getInstance(tableFieldInfo.getPropertyType(), tableFieldInfo.getTypeHandler());
                // todo 这会有影响 registry.register(typeHandler);
            }
            builder.typeHandler(typeHandler);
        }
        return builder.build();
    }


}
