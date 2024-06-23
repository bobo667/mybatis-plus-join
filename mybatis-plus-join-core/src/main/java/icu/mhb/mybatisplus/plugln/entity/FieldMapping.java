package icu.mhb.mybatisplus.plugln.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 字段映射
 *
 * @author mahuibo
 * @Title: FieldMapping
 * @email mhb0409@qq.com
 * @time 2021/12/17
 */
@Data
@AllArgsConstructor
public class FieldMapping {

    /**
     * 字段名
     */
    private String column;

    /**
     * 原始字段名
     */
    private String rawColumn;

    /**
     * 表別名
     */
    private String tableAlias;

    /**
     * 属性名
     */
    private String fieldName;

    private TableFieldInfoExt tableFieldInfoExt;

    public FieldMapping(String column, String fieldName, TableFieldInfoExt tableFieldInfoExt) {
        this.column = column;
        this.fieldName = fieldName;
        this.tableFieldInfoExt = tableFieldInfoExt;
    }

}
