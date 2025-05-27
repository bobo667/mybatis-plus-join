package icu.mhb.mybatisplus.plugln.core.str.util;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.entity.FieldMapping;
import icu.mhb.mybatisplus.plugln.entity.TableFieldInfoExt;

import java.util.List;
import java.util.Map;

/**
 * 字符串查询包装器工具类
 * 用于封装通用的字段处理逻辑，减少代码重复
 *
 * @author mahuibo
 * @Title: StrQueryWrapperHelper
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
public final class StrQueryWrapperHelper {

    private StrQueryWrapperHelper() {
        // 工具类不允许实例化
    }

    /**
     * 处理字段前缀
     * 如果字段没有包含点号"."，则添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @return 处理后的字段名
     */
    public static String handleColumnPrefix(String alias, String column) {
        if (StringUtils.isNotBlank(column)) {
            // 如果字段中已包含点号，说明已有前缀，不处理
            if (column.contains(StringPool.DOT)) {
                return column;
            }

            // 如果是带有AS的字段，只处理AS前面的部分
            if (column.toLowerCase().contains(" as ")) {
                String[] parts = column.split("(?i) as ");
                String field = parts[0].trim();
                String asAlias = parts[1].trim();

                // 如果字段部分不包含点号，添加表别名前缀
                if (!field.contains(StringPool.DOT)) {
                    return alias + StringPool.DOT + field + " AS " + asAlias;
                }
                return column;
            }

            // 普通字段，直接添加表别名前缀
            return alias + StringPool.DOT + column;
        }
        return column;
    }

    /**
     * 检查并处理字段名
     * 如果字段名不包含点号，则自动添加主表别名前缀
     *
     * @param column           字段名
     * @param masterTableAlias 主表别名
     * @return 处理后的字段名
     */
    public static String checkAndHandleColumn(String column, String masterTableAlias) {
        if (StringUtils.isNotBlank(column) && !column.contains(StringPool.DOT)) {
            return handleColumnPrefix(masterTableAlias, column);
        }
        return column;
    }

    /**
     * 根据字段名获取TableFieldInfo
     *
     * @param fieldName 字段名
     * @param clazz     类
     * @return TableFieldInfo
     */
    public static TableFieldInfo getTableFieldInfoByFieldName(String fieldName, Class<?> clazz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        if (tableInfo == null) {
            return null;
        }
        return tableInfo.getFieldList().stream()
                .filter(i -> i.getProperty().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据字段名获取TableFieldInfo
     *
     * @param fieldName 字段名
     * @param clazz     类
     * @return TableFieldInfo
     */
    public static TableFieldInfo getTableFieldInfoByColumn(String fieldName, Class<?> clazz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        if (tableInfo == null) {
            return null;
        }
        return tableInfo.getFieldList().stream()
                .filter(i -> i.getColumn().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据字段名获取TableFieldInfo
     *
     * @param fieldName 字段名
     * @param tableName 表名
     * @return TableFieldInfo
     */
    public static TableFieldInfo getTableFieldInfoByColumn(String fieldName, String tableName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        if (tableInfo == null) {
            return null;
        }

        return tableInfo.getFieldList().stream()
                .filter(i -> i.getColumn().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 创建字段映射
     *
     * @param fieldName      字段名
     * @param columns        列名
     * @param clz            类
     * @param alias2table    别名映射表
     * @param masterTableAlias 主表别名
     * @return FieldMapping
     */
    public static FieldMapping createFieldMapping(String fieldName, String columns, Class<?> clz, 
                                                   Map<String, String> alias2table, String masterTableAlias) {
        TableFieldInfoExt fieldInfoExt = null;
        String tableAlias = masterTableAlias;
        String rawColumn = columns;
        String column = fieldName;

        // 解析字段的表别名和原始字段名
        if (columns.contains(StringPool.DOT)) {
            String[] fieldParts = columns.split("\\.");
            tableAlias = fieldParts[0];
            rawColumn = fieldParts[1];
        }

        // 如果是带AS的字段，需要进一步解析
        if (columns.toLowerCase().contains(" as ")) {
            String[] parts = columns.split("(?i) as ");
            String originalField = parts[0].trim();
            column = parts[1].trim(); // 别名作为column

            // 解析原始字段的表别名和字段名
            if (originalField.contains(StringPool.DOT)) {
                String[] fieldParts = originalField.split("\\.");
                tableAlias = fieldParts[0];
                rawColumn = fieldParts[1]; // 原始字段名作为rawColumn
            } else {
                rawColumn = originalField; // 原始字段名作为rawColumn
            }
        }

        // 查找TypeHandler信息，使用原始字段名查找
        TableFieldInfo info = null;
        if (clz == null) {
            String tableName = alias2table.get(tableAlias);
            info = getTableFieldInfoByColumn(rawColumn, tableName);
        } else {
            info = getTableFieldInfoByColumn(rawColumn, clz);
        }
        
        if (info != null && (info.getTypeHandler() != null || info.getJdbcType() != null)) {
            fieldInfoExt = new TableFieldInfoExt(info);
            fieldInfoExt.setColumn(column); // 设置别名
            fieldInfoExt.setProperty(fieldName); // 设置属性名
        }

        // 创建字段映射：column是别名，rawColumn是原始字段名
        return new FieldMapping(column, rawColumn, tableAlias, fieldName, fieldInfoExt);
    }

    /**
     * 构建字段映射列表 - 用于oneToOne和manyToMany
     *
     * @param tableNameOrAlias 表名或别名
     * @param autoAlias        是否自动生成别名
     * @param alias2table      别名映射表
     * @param columns          字段列表
     * @return 字段映射列表
     */
    public static List<FieldMapping> buildFieldMappingList(String tableNameOrAlias, boolean autoAlias, 
                                                            Map<String, String> alias2table, String... columns) {
        List<FieldMapping> fieldMappings = new java.util.ArrayList<>();
        String tableName = alias2table.getOrDefault(tableNameOrAlias, tableNameOrAlias);

        if (ArrayUtils.isEmpty(columns)) {
            // 如果没有指定字段，查询所有字段
            TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
            if (tableInfo != null) {
                // 添加主键字段
                if (tableInfo.havePK()) {
                    String keyColumn = tableInfo.getKeyColumn();
                    String keyProperty = tableInfo.getKeyProperty();
                    String columnAlias = autoAlias ? tableNameOrAlias + "_" + keyProperty : keyProperty;
                    fieldMappings.add(new FieldMapping(columnAlias, keyColumn, tableNameOrAlias, keyProperty, null));
                }

                // 添加普通字段
                for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                    if (fieldInfo.isSelect()) {
                        String column = fieldInfo.getColumn();
                        String property = fieldInfo.getProperty();
                        String columnAlias = autoAlias ? tableNameOrAlias + "_" + property : property;

                        // 检查是否有TypeHandler
                        TableFieldInfoExt fieldInfoExt = null;
                        if (fieldInfo.getTypeHandler() != null || fieldInfo.getJdbcType() != null) {
                            fieldInfoExt = new TableFieldInfoExt(fieldInfo);
                            fieldInfoExt.setColumn(columnAlias);
                            fieldInfoExt.setProperty(property);
                        }

                        fieldMappings.add(new FieldMapping(columnAlias, column, tableNameOrAlias, property, fieldInfoExt));
                    }
                }
            }
        } else {
            // 使用指定的字段
            for (String column : columns) {
                if (column.toLowerCase().contains(" as ")) {
                    // 处理带AS的字段
                    String[] parts = column.split("(?i) as ");
                    String originalField = parts[0].trim();
                    String aliasName = parts[1].trim();

                    TableFieldInfo fieldInfo = getTableFieldInfoByColumn(originalField, tableName);
                    TableFieldInfoExt fieldInfoExt = null;
                    if (fieldInfo != null) {
                        fieldInfoExt = new TableFieldInfoExt(fieldInfo);
                        fieldInfoExt.setColumn(aliasName);
                        fieldInfoExt.setProperty(aliasName);
                    }

                    fieldMappings.add(new FieldMapping(aliasName, originalField, tableNameOrAlias, aliasName, fieldInfoExt));
                } else {
                    TableFieldInfo fieldInfo = getTableFieldInfoByColumn(column, tableName);
                    TableFieldInfoExt fieldInfoExt = null;
                    String property = column;
                    String columnAlias = column;

                    if (fieldInfo != null) {
                        property = fieldInfo.getProperty();
                        if (autoAlias) {
                            columnAlias = tableNameOrAlias + "_" + property;
                        }

                        fieldInfoExt = new TableFieldInfoExt(fieldInfo);
                        fieldInfoExt.setColumn(columnAlias);
                        fieldInfoExt.setProperty(property);
                    } else if (autoAlias) {
                        columnAlias = tableNameOrAlias + "_" + column;
                    }

                    fieldMappings.add(new FieldMapping(columnAlias, column, tableNameOrAlias, property, fieldInfoExt));
                }
            }
        }

        return fieldMappings;
    }

    /**
     * 根据字段映射列表添加查询字段
     *
     * @param fieldMappings 字段映射列表
     * @param sqlSelect     查询字段列表
     * @param fieldMappingList 字段映射列表
     */
    public static void addSelectByFieldMappings(List<FieldMapping> fieldMappings, 
                                                List<SharedString> sqlSelect, 
                                                List<FieldMapping> fieldMappingList) {
        for (FieldMapping mapping : fieldMappings) {
            String selectColumn;
            if (mapping.getTableAlias() != null && !mapping.getRawColumn().contains(StringPool.DOT)) {
                selectColumn = mapping.getTableAlias() + StringPool.DOT + mapping.getRawColumn();
            } else {
                selectColumn = mapping.getRawColumn();
            }

            // 如果字段名和别名不同，添加AS
            if (!mapping.getColumn().equals(mapping.getRawColumn())) {
                selectColumn += " AS " + mapping.getColumn();
            }

            sqlSelect.add(new SharedString(selectColumn));
            fieldMappingList.add(mapping);
        }
    }
} 