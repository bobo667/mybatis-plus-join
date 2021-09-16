package com.github.mybatisplus.plugln.entity;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Getter;

/**
 * 查询列表构建实体
 *
 * @param <T>
 */
@Getter
public class ColumnsBuilder<T> {

    /**
     * 列
     */
    SFunction<T, ?> column;

    /**
     * 用户自定义列
     */
    Object columnStr;

    /**
     * 别名
     */
    String alias;


    /**
     * 不需要别名 使用字段的映射
     *
     * @param column 字段
     */
    public ColumnsBuilder(SFunction<T, ?> column) {
        this.column = column;
        this.alias = "";
        this.columnStr = "";
    }

    /**
     * 设置字段名 并添加别名
     *
     * @param column 字段
     * @param alias  别名
     */
    public ColumnsBuilder(SFunction<T, ?> column, String alias) {
        this.column = column;
        this.alias = alias;
        this.columnStr = "";
    }

    /**
     * 自定义查询字段
     * 例如：
     *   ColumnsBuilder(1, "select_type")
     *   select 1 as select_type.....
     *
     * @param columnStr 字段
     * @param alias     别名
     */
    public ColumnsBuilder(Object columnStr, String alias) {
        this.column = null;
        this.alias = alias;
        this.columnStr = columnStr;
    }

}
