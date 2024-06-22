package icu.mhb.mybatisplus.plugln.entity;

import icu.mhb.mybatisplus.plugln.annotations.JoinChainModel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author mahuibo
 * @Title: ChainData
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@Data
@JoinChainModel
@AllArgsConstructor
public class ChainFieldData {

    /**
     * 字段属性
     */
    private String property;

    /**
     * 字段列名
     */
    private String column;

    /**
     * 字段别名
     */
    private String alias;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 模型class
     */
    private Class<?> modelClass;

    /**
     * 值
     */
    private Object val;

}
